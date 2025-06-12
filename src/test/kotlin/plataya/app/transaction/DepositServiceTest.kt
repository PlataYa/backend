package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.dtos.externalwallet.ExternalWalletValidationDTO

class DepositServiceTest : BaseTransactionServiceTest() {

    @Test
    fun `createDeposit success`() {
        val depositAmount = 200.0f
        val sourceCvu = 999L
        
        // Configure external wallet client
        val validationResponse = ExternalWalletValidationDTO(
            cvu = sourceCvu,
            exists = true,
            balance = 1000.0f,
            hasSufficientFunds = true
        )
        mockExternalWalletClient.configureBalanceValidation(sourceCvu, depositAmount, validationResponse)
        
        val response = createDeposit(wallet1.cvu, depositAmount, sourceCvu)

        assertExternalTransactionBasics(response, TransactionType.DEPOSIT, depositAmount)
        assertEquals(wallet1.cvu, response.destinationCvu)
        assertEquals(sourceCvu, response.sourceCvu)
        assertEquals("DEPOSIT_REF", response.externalReference)

        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 + depositAmount)
    }

    @Test
    fun `createDeposit wallet not found`() {
        assertThrows<WalletNotFoundException> {
            createDeposit(999L, 100.0f, 888L)
        }
    }

    @Test
    fun `createDeposit negative amount`() {
        assertThrows<InvalidTransactionException> {
            createDeposit(wallet1.cvu, -100.0f, 999L)
        }
    }

    @Test
    fun `createDeposit zero amount`() {
        assertThrows<InvalidTransactionException> {
            createDeposit(wallet1.cvu, 0.0f, 999L)
        }
    }
} 