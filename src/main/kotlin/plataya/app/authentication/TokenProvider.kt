package plataya.app.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey


@Component
class TokenProvider(
    @Value("\${JWT_SECRET_KEY}") private val secret: String
) {
    private lateinit var key: SecretKey

    @PostConstruct
    fun init() {
        if (secret.length < 32) {
            throw IllegalArgumentException("El secreto JWT debe tener al menos 256 bits (32 bytes)")
        }
        key = Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(userId: String, email: String, name: String, lastname: String): String {
        val now = Date()
        val expiry = Date(now.time + 86_400_000) // 1 día
        return Jwts.builder()
            .setSubject(email)
            .claim("userId", userId)
            .claim("name", name)
            .claim("lastname", lastname)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateTokenAndGetUsername(token: String): String? = try {
        val claims: Claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
        claims.subject
    } catch (ex: Exception) {
        null
    }
}
