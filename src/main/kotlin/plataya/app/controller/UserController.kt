package plataya.app.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.yaml.snakeyaml.nodes.Tag
import plataya.app.service.UserService
import plataya.app.model.dtos.UserDtoResponse
import plataya.app.model.dtos.UserDtoRequest
import com.jayway.jsonpath.internal.function.Parameter

@RestController
@RequestMapping("/v1/user")
class UserController(private val userService: UserService){

    @PostMapping("/register")
    fun register(@RequestBody userDto: UserDtoRequest): ResponseEntity<UserDtoResponse> {
        val created = userService.register(userDto.name, userDto.lastname, userDto.mail, userDto.password, userDto.dayOfBirth)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }

    @PostMapping("/login")
    fun login(@RequestBody userDto: UserDtoRequest): ResponseEntity<UserDtoResponse> {
        val created = userService.loginUser(userDto.mail, userDto.password)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }
    
}
