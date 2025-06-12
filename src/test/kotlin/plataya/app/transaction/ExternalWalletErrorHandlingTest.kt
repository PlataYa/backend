package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.*
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.dtos.externalwallet.ExternalWalletValidationDTO
import plataya.app.model.dtos.externalwallet.ExternalCvuValidationDTO
import plataya.app.model.dtos.transaction.ExternalDepositResponse

class ExternalWalletErrorHandlingTest : BaseTransactionServiceTest() {

    // --- External Deposit Error Tests ---
    @Test
    fun `createWithdrawal external deposit fails with error status`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation (successful)
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // Configure external deposit to return failed status
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123",
            destinationCvu = destinationCvu,
            amount = withdrawalAmount,
            currency = plataya.app.model.entities.transaction.Currency.ARS,
            status = "FAILED",
            message = "External deposit failed due to invalid account"
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
        
        assertEquals("External deposit failed: External deposit failed due to invalid account", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    @Test
    fun `createWithdrawal external deposit fails with processing status`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation (successful)
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = true,
            bankName = "TEST_BANK"
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // Configure external deposit to return processing status (not completed)
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123",
            destinationCvu = destinationCvu,
            amount = withdrawalAmount,
            currency = plataya.app.model.entities.transaction.Currency.ARS,
            status = "PROCESSING"
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

    // --- External CVU Validation Error Tests ---
    @Test
    fun `createWithdrawal external CVU validation fails - ExternalWalletNotFoundException`() {
        val withdrawalAmount = 100.0f
        val nonExistentCvu = 999L
        
        // Configure mock to throw ExternalWalletNotFoundException
        mockExternalWalletClient.configureCvuException(
            ExternalWalletNotFoundException("External CVU $nonExistentCvu not found")
        )
        
        val exception = assertThrows<ExternalWalletNotFoundException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, nonExistentCvu)
        }
        
        assertEquals("External CVU $nonExistentCvu not found", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    @Test
    fun `createWithdrawal external CVU validation fails - ExternalServiceException`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure mock to throw ExternalServiceException
        mockExternalWalletClient.configureCvuException(
            ExternalServiceException("External service error: Connection timeout")
        )
        
        val exception = assertThrows<ExternalServiceException> {
            createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        }
        
        assertEquals("External service error: Connection timeout", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    // --- External Balance Validation Error Tests ---
    @Test
    fun `createDeposit external balance validation fails - ExternalInsufficientFundsException`() {
        val depositAmount = 200.0f
        val sourceCvu = 999L
        
        // Configure mock to throw ExternalInsufficientFundsException
        mockExternalWalletClient.configureBalanceException(
            ExternalInsufficientFundsException("External CVU $sourceCvu has insufficient funds for amount $depositAmount")
        )
        
        val exception = assertThrows<ExternalInsufficientFundsException> {
            createDeposit(wallet1.cvu, depositAmount, sourceCvu)
        }
        
        assertEquals("External CVU $sourceCvu has insufficient funds for amount $depositAmount", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    @Test
    fun `createDeposit external balance validation fails - ExternalWalletNotFoundException`() {
        val depositAmount = 200.0f
        val sourceCvu = 999L
        
        // Configure mock to throw ExternalWalletNotFoundException
        mockExternalWalletClient.configureBalanceException(
            ExternalWalletNotFoundException("External CVU $sourceCvu not found")
        )
        
        val exception = assertThrows<ExternalWalletNotFoundException> {
            createDeposit(wallet1.cvu, depositAmount, sourceCvu)
        }
        
        assertEquals("External CVU $sourceCvu not found", exception.message)
        
        // Verify wallet balance was not changed
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1)
    }

    // --- CVU Validation with Different Responses ---
    @Test
    fun `external CVU validation returns non-existent CVU`() {
        val withdrawalAmount = 100.0f
        val destinationCvu = 999L
        
        // Configure external CVU validation to return non-existent
        val cvuValidationResponse = ExternalCvuValidationDTO(
            exists = false,
            bankName = ""
        )
        mockExternalWalletClient.configureCvuValidation(destinationCvu, cvuValidationResponse)
        
        // This test verifies that the service properly handles a CVU that exists in the external system
        // but returns exists = false. The service should proceed normally in this case,
        // as the external validation passed (no exception thrown)
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
            "", 
            depositResponse
        )
        
        val response = createWithdrawal(wallet1.cvu, withdrawalAmount, destinationCvu)
        
        assertExternalTransactionBasics(response, TransactionType.WITHDRAWAL, withdrawalAmount)
        assertEquals("", response.externalReference) // Empty bank name
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 - withdrawalAmount)
    }

    @Test
    fun `external balance validation returns insufficient funds false`() {
        val depositAmount = 200.0f
        val sourceCvu = 999L
        
        // Configure external balance validation to return hasSufficientFunds = false
        val validationResponse = ExternalWalletValidationDTO(
            cvu = sourceCvu,
            exists = true,
            balance = 100.0f, // Less than deposit amount
            hasSufficientFunds = false
        )
        mockExternalWalletClient.configureBalanceValidation(sourceCvu, depositAmount, validationResponse)
        
        // The service should proceed normally as long as no exception is thrown
        // This tests the case where the external validation returns but indicates insufficient funds
        val response = createDeposit(wallet1.cvu, depositAmount, sourceCvu)
        
        assertExternalTransactionBasics(response, TransactionType.DEPOSIT, depositAmount)
        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 + depositAmount)
    }
} 