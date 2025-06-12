package plataya.app

import io.github.cdimascio.dotenv.Dotenv
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

class TestEnvironmentInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        // Load .env file for tests if it exists
        if (Files.exists(Paths.get(".env"))) {
            val dotenv = Dotenv.load()
            val envMap = mutableMapOf<String, Any>()

            dotenv.entries().forEach { entry ->
                envMap[entry.key] = entry.value
                // Also set as system property for compatibility
                System.setProperty(entry.key, entry.value)
            }

            // Add properties to Spring Environment
            val propertySource = MapPropertySource("dotenv", envMap)
            applicationContext.environment.propertySources.addFirst(propertySource)
        }
    }
}
