package plataya.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import plataya.app.model.dto.UserDTO
import plataya.app.model.entity.User
import plataya.app.repository.UserRepository

@RestController
@RequestMapping("/test")
class TestController(private val userRepository: UserRepository) {
    @GetMapping("/ping")
    fun ping(): String {
        return "pong"
    }

    @PostMapping("/user-test")
    fun dbInsertion(@RequestBody userDTO: UserDTO): ResponseEntity<Any> {
        try {
            // Verify if the user already exists
            val userExists = userRepository.findByMail(userDTO.mail)
            if (userExists != null) {
                return ResponseEntity.badRequest().body(mapOf("error" to "Mail already registered"))
            }

            // Create and save the user
            val user = User(
                name = userDTO.name,
                surname = userDTO.surname,
                mail = userDTO.mail,
                password = userDTO.password // To be encripted later
            )

            val savedUser = userRepository.save(user)
            return ResponseEntity.ok(mapOf("mensaje" to "User created successfully", "id" to savedUser.id))
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/user-test")
    fun dbTest(): ResponseEntity<Any> {
        try {
            val users = userRepository.findAll()
            return ResponseEntity.ok(users)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(mapOf("error" to e.message))
        }
    }
}