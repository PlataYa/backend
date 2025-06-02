package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.TransactionType

class DepositServiceTest : BaseTransactionServiceTest() {

    @Test
    fun `createDeposit success`() {
        val depositAmount = 200.0f
        val response = createDeposit(wallet1.cvu, depositAmount, "ref123")

        assertTransactionBasics(response, TransactionType.DEPOSIT, depositAmount)
        assertEquals(wallet1.cvu, response.payeeCvu)
        assertNull(response.payerCvu)
        assertEquals("ref123", response.externalReference)

        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 + depositAmount)
        assertTransactionSaved(response.transactionId, depositAmount)
    }

    @Test
    fun `createDeposit wallet not found`() {
        assertThrows<WalletNotFoundException> {
            createDeposit(999L, 100.0f, "ref123")
        }
    }

    @Test
    fun `createDeposit negative amount`() {
        assertThrows<InvalidTransactionException> {
            createDeposit(wallet1.cvu, -100.0f, "ref123")
        }
    }

    @Test
    fun `createDeposit zero amount`() {
        assertThrows<InvalidTransactionException> {
            createDeposit(wallet1.cvu, 0.0f, "ref123")
        }
    }
} 