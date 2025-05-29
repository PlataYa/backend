package plataya.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import plataya.app.dto.UserDTO
import plataya.app.entity.User
import plataya.app.repository.UserRepository

@RestController
@RequestMapping("/test")
class TestController(private val userRepository: UserRepository) {
    @GetMapping("/ping")
    fun ping(): String {
        return "pong"
    }
}