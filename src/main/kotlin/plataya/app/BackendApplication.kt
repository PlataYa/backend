package plataya.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.github.cdimascio.dotenv.Dotenv

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>) {
	// Load environment variables from .env file
	val dotenv = Dotenv.configure().load()
	dotenv.entries().forEach { entry ->
		System.setProperty(entry.key, entry.value)
	}
	runApplication<BackendApplication>(*args)
}
