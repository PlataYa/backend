package plataya.app.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.stereotype.Service
import plataya.app.model.entities.User
import plataya.app.model.dtos.UserDtoResponse
import plataya.app.repository.UserRepository
import plataya.app.authentication.TokenProvider
import jakarta.persistence.EntityNotFoundException

@Service
class UserService(
    private val userRepository: UserRepository,  
    private val passwordEncoder: PasswordEncoder, 
    private val tokenProvider: TokenProvider
): UserDetailsService {
    
    override fun loadUserByUsername(email: String?): UserDetails {
        if (email == null) {
            throw UsernameNotFoundException("Email cannot be null")
        }
        
        val user = userRepository.findByMail(email)
            ?: throw UsernameNotFoundException("Usuario no encontrado con email: $email")
        
        return SpringUser.builder()
            .username(user.mail)
            .password(user.password)
            .authorities(emptyList())
            .build()
    }

    fun register(name: String, lastname: String, mail: String, password: String, dayOfBirth: String): UserDtoResponse {
        if(userRepository.findByMail(mail) != null){
            throw IllegalArgumentException("El email ya está registrado")
        }

        val user = User(
            name = name,
            lastname = lastname,
            mail = mail,
            password = passwordEncoder.encode(password),
            dayOfBirth = dayOfBirth
        )

        val savedUser = userRepository.save(user)
        val userDTO = translateUserToUserDtoResponse(savedUser)
        val token = tokenProvider.generateToken(savedUser.id.toString(), savedUser.mail, savedUser.name, savedUser.lastname)
        userDTO.token = token
        
        return userDTO
    }

    private fun translateUserToUserDtoResponse(user: User): UserDtoResponse {
        return UserDtoResponse(
            name = user.name,
            lastname = user.lastname,
            mail = user.mail,
        )
    }
    
    fun loginUser(email: String, password: String): UserDtoResponse {
        val user = userRepository.findByMail(email)
            ?: throw EntityNotFoundException("Usuario no encontrado")
    
        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Contraseña no válida")
        }
    
        val userDTO = translateUserToUserDtoResponse(user)
        val token = tokenProvider.generateToken(user.id.toString(), user.mail, user.name, user.lastname)
        userDTO.token = token
    
        return userDTO
    }
}
