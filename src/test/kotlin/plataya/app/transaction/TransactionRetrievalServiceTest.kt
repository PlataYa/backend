package plataya.app.transaction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.dtos.externalwallet.ExternalWalletValidationDTO
import plataya.app.model.dtos.externalwallet.ExternalCvuValidationDTO
import plataya.app.model.dtos.transaction.ExternalDepositResponse

class TransactionRetrievalServiceTest : BaseTransactionServiceTest() {

    // --- Single Transaction Retrieval Tests ---
    @Test
    fun `getTransactionById P2P success`() {
        val response = createP2PTransfer(wallet1.cvu, wallet2.cvu, 100.0f)
        
        val retrievedTransaction = transactionService.getTransactionById(response.transactionId)
        
        assertEquals(response.transactionId, retrievedTransaction.transactionId)
        assertEquals(response.type, retrievedTransaction.type)
        assertEquals(response.amount, retrievedTransaction.amount)
        assertEquals(response.payerCvu, retrievedTransaction.payerCvu)
        assertEquals(response.payeeCvu, retrievedTransaction.payeeCvu)
        assertEquals(response.status, retrievedTransaction.status)
    }

    @Test
    fun `getTransactionById external transaction success`() {
        val depositAmount = 200.0f
        val sourceCvu = 999L
        
        // Configure external wallet client for deposit
        val validationResponse = ExternalWalletValidationDTO(
            cvu = sourceCvu,
            exists = true,
            balance = 1000.0f,
            hasSufficientFunds = true
        )
        mockExternalWalletClient.configureBalanceValidation(sourceCvu, depositAmount, validationResponse)
        
        val response = createDeposit(wallet1.cvu, depositAmount, sourceCvu)
        
        val retrievedTransaction = transactionService.getTransactionById(response.transactionId)
        
        assertEquals(response.transactionId, retrievedTransaction.transactionId)
        assertEquals(response.type, retrievedTransaction.type)
        assertEquals(response.amount, retrievedTransaction.amount)
        // For external transactions, the response is converted to TransactionResponse format
        // where deposits have payerCvu = sourceCvu, payeeCvu = destinationCvu
        assertEquals(sourceCvu, retrievedTransaction.payerCvu)
        assertEquals(wallet1.cvu, retrievedTransaction.payeeCvu)
        assertEquals(response.status, retrievedTransaction.status)
    }

    @Test
    fun `getTransactionById not found`() {
        assertThrows<InvalidTransactionException> {
            transactionService.getTransactionById(999L)
        }
    }

    // --- Get Wallet Transaction History Tests ---
    @Test
    fun `getWalletTransactionHistory success with multiple transactions`() {
        // Configure external client for deposit
        val depositValidationResponse = ExternalWalletValidationDTO(
            cvu = 999L,
            exists = true,
            balance = 1000.0f,
            hasSufficientFunds = true
        )
        mockExternalWalletClient.configureBalanceValidation(999L, 200.0f, depositValidationResponse)
        
        // Configure external client for withdrawal
        val validationResponse = ExternalCvuValidationDTO(exists = true, bankName = "TEST_BANK")
        mockExternalWalletClient.configureCvuValidation(888L, validationResponse)
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123", destinationCvu = 888L, amount = 75.0f,
            currency = plataya.app.model.entities.transaction.Currency.ARS, status = "COMPLETED"
        )
        mockExternalWalletClient.configureDepositResponse(
            888L, 75.0f, plataya.app.model.entities.transaction.Currency.ARS, "TEST_BANK", depositResponse
        )
        
        createP2PTransfer(wallet1.cvu, wallet2.cvu, 50.0f)
        createDeposit(wallet1.cvu, 200.0f, 999L)
        createWithdrawal(wallet1.cvu, 75.0f, 888L)

        val transactions = transactionService.getWalletTransactionHistory(wallet1.cvu)

        assertEquals(3, transactions.size)

        // Verify sorting (newest first)
        assertTrue(transactions[0].createdAt >= transactions[1].createdAt)
        assertTrue(transactions[1].createdAt >= transactions[2].createdAt)

        // Verify all transactions involve wallet1
        transactions.forEach { transaction ->
            assertTrue(
                transaction.payerCvu == wallet1.cvu || transaction.payeeCvu == wallet1.cvu,
                "Transaction should involve wallet1 CVU: ${wallet1.cvu}"
            )
        }

        val transactionTypes = transactions.map { it.type }
        assertTrue(transactionTypes.contains(TransactionType.P2P))
        assertTrue(transactionTypes.contains(TransactionType.DEPOSIT))
        assertTrue(transactionTypes.contains(TransactionType.WITHDRAWAL))
    }

    @Test
    fun `getWalletTransactionHistory success with no transactions`() {
        val transactions = transactionService.getWalletTransactionHistory(wallet2.cvu)
        
        assertEquals(0, transactions.size)
        assertTrue(transactions.isEmpty())
    }

    @Test
    fun `getWalletTransactionHistory wallet not found`() {
        assertThrows<WalletNotFoundException> {
            transactionService.getWalletTransactionHistory(999L)
        }
    }

    @Test
    fun `getWalletTransactionHistory only shows transactions for specific wallet`() {
        // Configure external client for deposit
        val depositValidationResponse = ExternalWalletValidationDTO(
            cvu = 999L,
            exists = true,
            balance = 1000.0f,
            hasSufficientFunds = true
        )
        mockExternalWalletClient.configureBalanceValidation(999L, 300.0f, depositValidationResponse)
        
        createP2PTransfer(wallet1.cvu, wallet2.cvu, 100.0f)
        createDeposit(wallet2.cvu, 300.0f, 999L)

        val wallet1Transactions = transactionService.getWalletTransactionHistory(wallet1.cvu)
        
        assertEquals(1, wallet1Transactions.size)
        assertEquals(TransactionType.P2P, wallet1Transactions[0].type)
        assertEquals(wallet1.cvu, wallet1Transactions[0].payerCvu)

        val wallet2Transactions = transactionService.getWalletTransactionHistory(wallet2.cvu)
        
        assertEquals(2, wallet2Transactions.size)
        wallet2Transactions.forEach { transaction ->
            assertEquals(wallet2.cvu, transaction.payeeCvu)
        }
    }

    @Test
    fun `getWalletTransactionHistory mixed transaction types for single wallet`() {
        // Configure external client for deposit
        val depositValidationResponse = ExternalWalletValidationDTO(
            cvu = 999L,
            exists = true,
            balance = 1000.0f,
            hasSufficientFunds = true
        )
        mockExternalWalletClient.configureBalanceValidation(999L, 200.0f, depositValidationResponse)
        
        // Configure external client for withdrawal 
        val validationResponse = ExternalCvuValidationDTO(exists = true, bankName = "TEST_BANK")
        mockExternalWalletClient.configureCvuValidation(888L, validationResponse)
        val depositResponse = ExternalDepositResponse(
            transactionId = "EXT123", destinationCvu = 888L, amount = 50.0f,
            currency = plataya.app.model.entities.transaction.Currency.ARS, status = "COMPLETED"
        )
        mockExternalWalletClient.configureDepositResponse(
            888L, 50.0f, plataya.app.model.entities.transaction.Currency.ARS, "TEST_BANK", depositResponse
        )
        
        // Create various types of transactions involving wallet1
        createP2PTransfer(wallet1.cvu, wallet2.cvu, 100.0f)
        createDeposit(wallet1.cvu, 200.0f, 999L)
        createWithdrawal(wallet1.cvu, 50.0f, 888L)

        val transactions = transactionService.getWalletTransactionHistory(wallet1.cvu)

        assertEquals(3, transactions.size)

        // Check that all transactions involve wallet1
        transactions.forEach { transaction ->
            assertTrue(
                transaction.payerCvu == wallet1.cvu || transaction.payeeCvu == wallet1.cvu,
                "Transaction should involve wallet1: payerCvu=${transaction.payerCvu}, payeeCvu=${transaction.payeeCvu}, wallet1=${wallet1.cvu}"
            )
        }

        // Verify the transaction types present
        val transactionTypes = transactions.map { it.type }
        assertTrue(transactionTypes.contains(TransactionType.P2P))
        assertTrue(transactionTypes.contains(TransactionType.DEPOSIT))
        assertTrue(transactionTypes.contains(TransactionType.WITHDRAWAL))
    }
} 