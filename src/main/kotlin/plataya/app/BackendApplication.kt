package plataya.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>) {
	// Load environment variables
	DotenvConfig.loadEnv()
	runApplication<BackendApplication>(*args)
}
