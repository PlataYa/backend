package plataya.app.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import plataya.app.exception.InvalidCredentialsException
import plataya.app.model.dtos.user.LoginRequest
import plataya.app.service.UserService
import plataya.app.model.dtos.user.UserDtoResponse
import plataya.app.model.dtos.user.UserDtoRequest

@RestController
@RequestMapping("/api/v1/user")
class UserController(private val userService: UserService){

    @PostMapping("/register")
    fun register(@RequestBody userDto: UserDtoRequest): ResponseEntity<UserDtoResponse> {
        val created = userService.register(userDto.name, userDto.lastname, userDto.mail, userDto.password, userDto.dayOfBirth)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }

    @PostMapping("/login")
    fun login(@RequestBody userDto: LoginRequest): ResponseEntity<UserDtoResponse> {
        val created = userService.loginUser(userDto.mail, userDto.password)
        return ResponseEntity.status(HttpStatus.OK).body(created)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message ?: "Bad Request")
    }
}
