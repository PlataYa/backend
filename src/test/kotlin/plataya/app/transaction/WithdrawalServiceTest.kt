package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.dtos.externalwallet.ExternalCvuValidationDTO
import plataya.app.model.dtos.transaction.ExternalDepositResponse

class WithdrawalServiceTest : BaseTransactionServiceTest() {

    @Test
    fun `createWithdrawal success`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external wallet client
        val validationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, validationResponse)
        
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123",
            destinationCvu = destinationCvu,
            amount = withdrawalAmount,
            currency = plataya.app.model.entities.transaction.Currency.ARS,
            status = "COMPLETED"
        )
        mockExternalWalletClient.configureDepositResponse(
            destinationCvu, 
            withdrawalAmount, 
            plataya.app.model.entities.transaction.Currency.ARS, 
            "TEST_BANK", 
            depositResponse
        )
        
        val response = createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)

        assertExternalTransactionBasics(response, TransactionType.WITHDRAWAL, withdrawalAmount)
        assertEquals(wallet1.cvu, response.sourceCvu)
        assertEquals(destinationCvu, response.destinationCvu)
        assertEquals("TEST_BANK", response.externalReference)

        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 - withdrawalAmount)
    }

    @Test
    fun `createWithdrawal wallet not found`() {
        assertThrows<WalletNotFoundException> {
            createWithdrawal(999L, 100.0f, 888L)
        }
    }

    @Test
    fun `createWithdrawal insufficient funds`() {
        assertThrows<InsufficientFundsException> {
            createWithdrawal(wallet1.cvu, initialBalanceWallet1 + 100f, 999L)
        }
    }

    @Test
    fun `createWithdrawal negative amount`() {
        assertThrows<InvalidTransactionException> {
            createWithdrawal(wallet1.cvu, -100.0f, 999L)
        }
    }

    @Test
    fun `createWithdrawal zero amount`() {
        assertThrows<InvalidTransactionException> {
            createWithdrawal(wallet1.cvu, 0.0f, 999L)
        }
    }

    @Test
    fun `createWithdrawal exact balance amount`() {
        val destinationCvu = 999L
        
        // Configure external wallet client
        val validationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, validationResponse)
        
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123",
            destinationCvu = destinationCvu,
            amount = initialBalanceWallet1,
            currency = plataya.app.model.entities.transaction.Currency.ARS,
            status = "COMPLETED"
        )
        mockExternalWalletClient.configureDepositResponse(
            destinationCvu, 
            initialBalanceWallet1, 
            plataya.app.model.entities.transaction.Currency.ARS, 
            "TEST_BANK", 
            depositResponse
        )
        
        val response = createWithdrawal(wallet1.cvu, initialBalanceWallet1, destinationCvu)

        assertExternalTransactionBasics(response, TransactionType.WITHDRAWAL, initialBalanceWallet1)
        assertWalletBalance(wallet1.cvu, 0.0f)
    }
} 