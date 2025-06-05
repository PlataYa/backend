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
import plataya.app.repository.WalletRepository
import plataya.app.authentication.TokenProvider
import plataya.app.exception.InvalidCredentialsException

@Service
class UserService(
    private val userRepository: UserRepository,  
    private val passwordEncoder: PasswordEncoder, 
    private val tokenProvider: TokenProvider,
    private val walletService: WalletService,
    private val walletRepository: WalletRepository
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
            throw InvalidCredentialsException("Credenciales inválidas")
        }

        val user = User(
            name = name,
            lastname = lastname,
            mail = mail,
            password = passwordEncoder.encode(password),
            dayOfBirth = dayOfBirth
        )

        // Primero guardamos el usuario
        val savedUser = userRepository.save(user)
        
        // Generamos el token
        val token = tokenProvider.generateToken(savedUser.id.toString(), savedUser.mail, savedUser.name, savedUser.lastname)
        
        // Creamos el wallet
        val wallet = walletService.createWallet(savedUser)
        
        // Finalmente creamos el DTO con toda la información
        return UserDtoResponse(
            name = savedUser.name,
            lastname = savedUser.lastname,
            mail = savedUser.mail,
            cvu = wallet.cvu,
            token = token
        )
    }

    private fun translateUserToUserDtoResponse(user: User): UserDtoResponse {
        // Buscar el wallet del usuario
        val wallet = walletRepository.findByUser(user)
        
        return UserDtoResponse(
            name = user.name,
            lastname = user.lastname,
            mail = user.mail,
            cvu = wallet?.cvu
        )
    }
    
    fun loginUser(email: String, password: String): UserDtoResponse {
        val user = userRepository.findByMail(email)
            ?: throw InvalidCredentialsException("Credenciales inválidas")
    
        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidCredentialsException("Credenciales inválidas")
        }
    
        val userDTO = translateUserToUserDtoResponse(user)
        val token = tokenProvider.generateToken(user.id.toString(), user.mail, user.name, user.lastname)
        userDTO.token = token
        return userDTO
    }
}
