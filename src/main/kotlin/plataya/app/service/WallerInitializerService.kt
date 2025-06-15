package plataya.app.service

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.boot.context.event.ApplicationReadyEvent
import plataya.app.model.dtos.transaction.ExternalTransactionDTO
import plataya.app.model.dtos.user.UserDtoResponse
import plataya.app.model.entities.transaction.Currency

@Component
class WallerInitializerService(
    private val userService: UserService,
    private val transactionService: TransactionService
) {

    @EventListener(ApplicationReadyEvent::class)
    fun initWallets() {
        println(">> Inicializando cuentas de prueba...")

        val users = listOf(
            Triple("martina@mail.com", "Martina", 1500.0),
            Triple("elias@mail.com", "Elias", 2500.0),
            Triple("nacho@mail.com", "Nacho", 1000.0),
            Triple("ana@mail.com", "Ana", 300.0)
        )

        for ((email, name, balance) in users) {
            try {
                val user : UserDtoResponse = userService.register(
                    name = name,
                    lastname = "Test",
                    mail = email,
                    password = "Contraseña1!",
                    dayOfBirth = "2000-01-01"
                )
                transactionService.createDeposit(
                    ExternalTransactionDTO(
                        sourceCvu = "555".toLong(),
                        destinationCvu = user.cvu!!,
                        amount = balance.toFloat(),
                        currency = Currency.ARS,
                        externalReference = "Banco Macro",
                    )
                )
                println("✅ Cuenta creada para $email con saldo $balance")
            } catch (e: Exception) {
                println("⚠️ No se pudo crear usuario $email: ${e.message}")
            }
        }
    }
}
