package plataya.app.authentication

import jakarta.servlet.FilterChain
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import plataya.app.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.context.annotation.Lazy
import java.io.IOException


@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    @Lazy private val userService: UserService
) : OncePerRequestFilter() {

    companion object {
        private const val HEADER = "Authorization"
        private const val PREFIX = "Bearer "
    }

    @Throws(IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HEADER)
        if (header != null && header.startsWith(PREFIX)) {
            val token = header.removePrefix(PREFIX)
            val username = tokenProvider.validateTokenAndGetUsername(token)

            if (username != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userService.loadUserByUsername(username)
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }
        filterChain.doFilter(request, response)
    }
}