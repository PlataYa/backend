package plataya.app

import io.github.cdimascio.dotenv.Dotenv
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

object DotenvConfig {
    fun loadEnv() {
        // Check if running in Docker
        if (Files.exists(Paths.get(".dockerenv"))) {
            // Running in Docker, skip loading .env file
            return
        }

        // Check if .env file exists
        if (Files.exists(Paths.get(".env"))) {
            val dotenv = Dotenv.load()
            dotenv.entries().forEach { entry ->
                if (System.getProperty(entry.key) == null) {
                    System.setProperty(entry.key, entry.value)
                }
            }
        } else {
            // Fallback to system environment variables
            System.getenv().forEach { (key, value) ->
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value)
                }
            }
        }
    }
}

@Configuration
class WebConfig {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
            }
        }
    }
}

