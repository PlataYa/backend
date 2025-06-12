package plataya.app.transaction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.*
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.dtos.externalwallet.ExternalWalletValidationDTO
import plataya.app.model.dtos.externalwallet.ExternalCvuValidationDTO
import plataya.app.model.dtos.transaction.ExternalDepositResponse

class ExceptionTest : BaseTransactionServiceTest() {

    // --- Additional External Deposit Error Scenarios ---
    @Test
    fun `createWithdrawal external deposit service unavailable`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation (successful)
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // Configure external deposit to throw service exception
        mockExternalWalletClient.configureDepositException(
            ExternalServiceException("External deposit service unavailable")
        )
        
        val exception = assertThrows<ExternalServiceException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        }
        
        assertEquals("External deposit service unavailable", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    @Test
    fun `createWithdrawal external deposit CVU not found during deposit`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation (successful)
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // Configure external deposit to throw wallet not found
        mockExternalWalletClient.configureDepositException(
            ExternalWalletNotFoundException("External CVU $destinationCvu not found during deposit")
        )
        
        val exception = assertThrows<ExternalWalletNotFoundException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        }
        
        assertEquals("External CVU $destinationCvu not found during deposit", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    // --- Custom Exception Message Tests ---
    @Test
    fun `createWithdrawal external deposit fails with specific error message`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation (successful)
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // Configure external deposit to return failed status with specific message
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123",
            destinationCvu = destinationCvu,
            amount = withdrawalAmount,
            currency = plataya.app.model.entities.transaction.Currency.ARS,
            status = "FAILED",
            message = "Daily transaction limit exceeded"
        )
        mockExternalWalletClient.configureDepositResponse(
            destinationCvu, 
            withdrawalAmount, 
            plataya.app.model.entities.transaction.Currency.ARS, 
            "TEST_BANK", 
            depositResponse
        )
        
        val exception = assertThrows<InvalidTransactionException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        }
        
        assertEquals("External deposit failed: Daily transaction limit exceeded", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    @Test
    fun `createWithdrawal external deposit fails with null message`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation (successful)
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // Configure external deposit to return failed status with null message
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123",
            destinationCvu = destinationCvu,
            amount = withdrawalAmount,
            currency = plataya.app.model.entities.transaction.Currency.ARS,
            status = "FAILED",
            message = null
        )
        mockExternalWalletClient.configureDepositResponse(
            destinationCvu, 
            withdrawalAmount, 
            plataya.app.model.entities.transaction.Currency.ARS, 
            "TEST_BANK", 
            depositResponse
        )
        
        val exception = assertThrows<InvalidTransactionException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        }
        
        assertEquals("External deposit failed: Unknown error", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    // --- Exception Handling for Different Transaction Types ---
    @Test
    fun `all custom exceptions are properly defined`() {
        // Test that all custom exceptions can be instantiated and have proper messages
        val walletNotFound = WalletNotFoundException("Wallet not found")
        val insufficientFunds = InsufficientFundsException("Insufficient funds")
        val invalidTransaction = InvalidTransactionException("Invalid transaction")
        val transactionNotFound = TransactionNotFoundException("Transaction not found")
        val externalService = ExternalServiceException("External service error")
        val externalWalletNotFound = ExternalWalletNotFoundException("External wallet not found")
        val externalInsufficientFunds = ExternalInsufficientFundsException("External insufficient funds")

        // Verify messages
        assertEquals("Wallet not found", walletNotFound.message)
        assertEquals("Insufficient funds", insufficientFunds.message)
        assertEquals("Invalid transaction", invalidTransaction.message)
        assertEquals("Transaction not found", transactionNotFound.message)
        assertEquals("External service error", externalService.message)
        assertEquals("External wallet not found", externalWalletNotFound.message)
        assertEquals("External insufficient funds", externalInsufficientFunds.message)
    }

    // --- Edge Cases for Deposit Validation ---
    @Test
    fun `createDeposit with external service throwing generic exception`() {
        val depositAmount = 200.0f
        val sourceCvu = 999L
        
        // Configure mock to throw generic exception (not our custom ones)
        mockExternalWalletClient.configureBalanceException(
            RuntimeException("Generic connection error")
        )
        
        val exception = assertThrows<RuntimeException> {
            createDeposit(wallet1.cvu, depositAmount, sourceCvu)
        }
        
        assertEquals("Generic connection error", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    // --- Mixed Error Scenarios ---
    @Test
    fun `createWithdrawal succeeds after external client errors are cleared`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // First configure an error
        mockExternalWalletClient.configureCvuException(
            ExternalServiceException("Temporary error")
        )
        
        // Verify the error is thrown
        assertThrows<ExternalServiceException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        }
        
        // Clear the error and configure successful responses
        mockExternalWalletClient.clearAll()
        
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
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
        
        // Now the withdrawal should succeed
        val response = createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        
        assertExternalTransactionBasics(response, TransactionType.WITHDRAWAL, withdrawalAmount)
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 - withdrawalAmount)
    }

    // --- Transaction Not Found Exception Coverage ---
    @Test
    fun `getTransactionById throws InvalidTransactionException for non-existent transaction`() {
        val exception = assertThrows<InvalidTransactionException> {
            transactionService.getTransactionById(99999L)
        }
        
        assertTrue(exception.message?.contains("not found") == true)
    }
} 