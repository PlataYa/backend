package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.TransactionType

class WithdrawalServiceTest : BaseTransactionServiceTest() {

    @Test
    fun `createWithdrawal success`() {
        val withdrawalAmount = 100.0f
        val response = createWithdrawal(wallet1.cvu, withdrawalAmount, "ref456")

        assertTransactionBasics(response, TransactionType.WITHDRAWAL, withdrawalAmount)
        assertEquals(wallet1.cvu, response.payerCvu)
        assertNull(response.payeeCvu)
        assertEquals("ref456", response.externalReference)

        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 - withdrawalAmount)
        assertTransactionSaved(response.transactionId, withdrawalAmount)
    }

    @Test
    fun `createWithdrawal wallet not found`() {
        assertThrows<WalletNotFoundException> {
            createWithdrawal(999L, 100.0f, "ref456")
        }
    }

    @Test
    fun `createWithdrawal insufficient funds`() {
        assertThrows<InsufficientFundsException> {
            createWithdrawal(wallet1.cvu, initialBalanceWallet1 + 100f, "ref456")
        }
    }

    @Test
    fun `createWithdrawal negative amount`() {
        assertThrows<InvalidTransactionException> {
            createWithdrawal(wallet1.cvu, -100.0f, "ref456")
        }
    }

    @Test
    fun `createWithdrawal zero amount`() {
        assertThrows<InvalidTransactionException> {
            createWithdrawal(wallet1.cvu, 0.0f, "ref456")
        }
    }

    @Test
    fun `createWithdrawal exact balance amount`() {
        val response = createWithdrawal(wallet1.cvu, initialBalanceWallet1, "ref456")

        assertTransactionBasics(response, TransactionType.WITHDRAWAL, initialBalanceWallet1)
        assertWalletBalance(wallet1.cvu, 0.0f)
    }
} 