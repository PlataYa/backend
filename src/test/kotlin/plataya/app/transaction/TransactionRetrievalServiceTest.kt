package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.TransactionNotFoundException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.TransactionType

class TransactionRetrievalServiceTest : BaseTransactionServiceTest() {

    // --- Get Transaction By ID Tests ---
    @Test
    fun `getTransactionById success`() {
        val transferAmount = 50.0f
        val createdTransactionResponse = createP2PTransfer(wallet1.cvu, wallet2.cvu, transferAmount)

        val foundTransactionResponse = transactionService.getTransactionById(createdTransactionResponse.transactionId)

        assertNotNull(foundTransactionResponse)
        assertEquals(createdTransactionResponse.transactionId, foundTransactionResponse.transactionId)
        assertEquals(TransactionType.P2P, foundTransactionResponse.type)
        assertEquals(transferAmount, foundTransactionResponse.amount)
        assertEquals(wallet1.cvu, foundTransactionResponse.payerCvu)
        assertEquals(wallet2.cvu, foundTransactionResponse.payeeCvu)
    }

    @Test
    fun `getTransactionById not found`() {
        assertThrows<TransactionNotFoundException> {
            transactionService.getTransactionById(999L)
        }
    }

    // --- Get Wallet Transaction History Tests ---
    @Test
    fun `getWalletTransactionHistory success with multiple transactions`() {
        createP2PTransfer(wallet1.cvu, wallet2.cvu, 50.0f)
        createDeposit(wallet1.cvu, 200.0f, "dep123")
        createWithdrawal(wallet1.cvu, 75.0f, "with456")

        val transactions = transactionService.getTransactionsByCvu(wallet1.cvu)

        assertEquals(3, transactions.size)

        assertTrue(transactions[0].createdAt >= transactions[1].createdAt)
        assertTrue(transactions[1].createdAt >= transactions[2].createdAt)

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
        val transactions = transactionService.getTransactionsByCvu(wallet2.cvu)
        
        assertEquals(0, transactions.size)
        assertTrue(transactions.isEmpty())
    }

    @Test
    fun `getWalletTransactionHistory wallet not found`() {
        assertThrows<WalletNotFoundException> {
            transactionService.getTransactionsByCvu(999L)
        }
    }

    @Test
    fun `getWalletTransactionHistory only shows transactions for specific wallet`() {
        createP2PTransfer(wallet1.cvu, wallet2.cvu, 100.0f)
        createDeposit(wallet2.cvu, 300.0f, "dep789")

        val wallet1Transactions = transactionService.getTransactionsByCvu(wallet1.cvu)
        
        assertEquals(1, wallet1Transactions.size)
        assertEquals(TransactionType.P2P, wallet1Transactions[0].type)
        assertEquals(wallet1.cvu, wallet1Transactions[0].payerCvu)

        val wallet2Transactions = transactionService.getTransactionsByCvu(wallet2.cvu)
        
        assertEquals(2, wallet2Transactions.size)
        wallet2Transactions.forEach { transaction ->
            assertEquals(wallet2.cvu, transaction.payeeCvu)
        }
    }

    @Test
    fun `getWalletTransactionHistory mixed transaction types for single wallet`() {
        // Create various types of transactions involving wallet1
        createP2PTransfer(wallet1.cvu, wallet2.cvu, 100.0f)
        createDeposit(wallet1.cvu, 200.0f, "deposit_ref")
        createWithdrawal(wallet1.cvu, 50.0f, "withdrawal_ref")

        val transactions = transactionService.getTransactionsByCvu(wallet1.cvu)
        
        assertEquals(3, transactions.size)
        
        // Verify we have all types
        val types = transactions.map { it.type }.toSet()
        assertEquals(setOf(TransactionType.P2P, TransactionType.DEPOSIT, TransactionType.WITHDRAWAL), types)
        
        // Verify transaction details
        val p2pTransaction = transactions.find { it.type == TransactionType.P2P }!!
        assertEquals(wallet1.cvu, p2pTransaction.payerCvu)
        assertEquals(wallet2.cvu, p2pTransaction.payeeCvu)
        
        val depositTransaction = transactions.find { it.type == TransactionType.DEPOSIT }!!
        assertNull(depositTransaction.payerCvu)
        assertEquals(wallet1.cvu, depositTransaction.payeeCvu)
        assertEquals("deposit_ref", depositTransaction.externalReference)
        
        val withdrawalTransaction = transactions.find { it.type == TransactionType.WITHDRAWAL }!!
        assertEquals(wallet1.cvu, withdrawalTransaction.payerCvu)
        assertNull(withdrawalTransaction.payeeCvu)
        assertEquals("withdrawal_ref", withdrawalTransaction.externalReference)
    }
} 