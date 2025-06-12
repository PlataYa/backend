package plataya.app.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import plataya.app.TestEnvironmentInitializer
import plataya.app.repository.UserRepository
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestEnvironmentInitializer::class])
@ActiveProfiles("test")
class UserControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var userRepository: UserRepository
    val userJson = """{
                "name": "nacho",
                "lastname": "capo",
                "mail": "nacho@mail.com",
                "password": "ContraseñaSegura25",
                "dayOfBirth": "18-11-2003"
            }"""

    @Test
    fun `test ping pong endpoint`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/ping"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("pong"))
    }

    @Test
    @Transactional
    fun `when user is created, it should be saved in the database and a wallet should have been created`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.mail").value("nacho@mail.com"))

        // Check if the user was created in the database
        val user = userRepository.findByMail("nacho@mail.com")
        assertNotNull(user)
        assertEquals("nacho", user?.name)
    }

    @Test
    @Transactional
    fun `when user registers with an existing email, it should return error`() {
        // Initial registration
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        // Trying to register again with the same email
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @Transactional
    fun `when user logs in with correct credentials, it should return user and token`() {
        // Initial registration
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        // Login
        val loginJson = """{"mail":"nacho@mail.com","password":"ContraseñaSegura25"}"""
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
            .contentType("application/json")
            .content(loginJson))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.mail").value("nacho@mail.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
    }

    @Test
    @Transactional
    fun `when user logs in with wrong password, it should return error`() {
        // Initial registration
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        // Login with incorrect password
        val loginJson = """{"mail":"nacho@mail.com","password":"incorrecta"}"""
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
            .contentType("application/json")
            .content(loginJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `when user does not exist, login should return error`() {
        val loginJson = """{"mail":"noexiste@mail.com","password":"algo"}"""
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
            .contentType("application/json")
            .content(loginJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
