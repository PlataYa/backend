package plataya.app.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WalletControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc
    val userJson = """{
                "name": "nacho",
                "lastname": "capo",
                "mail": "nacho@mail.com",
                "password": "ContraseñaSegura25",
                "dayOfBirth": "18-11-2003"
            }"""
    val anotherUserJson = """{
                "name": "elias",
                "lastname": "capo",
                "mail": "elias@mail.com",
                "password": "ContraseñaSegura25",
                "dayOfBirth": "11-09-2003"
            }"""

    @Test
    @Transactional
    fun `get wallet by mail returns wallet correctly`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/mine")
            .contentType("application/json").content("""{ "mail": "nacho@mail.com" }"""))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.cvu").value(100000000001))
    }

    @Test
    @Transactional
    fun `when db empty, no wallets should be returned`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/all"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.wallets").isEmpty)
    }

    @Test
    @Transactional
    fun `when creating two users and asking for all wallets, should return both wallets`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(anotherUserJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/all"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.wallets").isNotEmpty)
    }

    @Test
    fun `Unexisting CVU, when trying to be validated, returns false`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/valid/cvu?cvu=1"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.valid").value(false))
    }

    @Test
    @Transactional
    fun `Existing CVU, when trying to be validated, returns true`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType("application/json")
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/valid/cvu?cvu=100000000001"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.valid").value(true))
    }

    @Test
    @Transactional
    fun `can not get balance if cvu is invalid`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/balance/1"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
