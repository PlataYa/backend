package plataya.app

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@ContextConfiguration(initializers = [TestEnvironmentInitializer::class])
class AppApplicationTests {

	@Test
	fun contextLoads() {
	}

}
