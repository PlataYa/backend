package plataya.app.transaction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.entities.transaction.TransactionType

class P2PTransferServiceTest : BaseTransactionServiceTest() {

    @Test
    fun `createP2PTransfer success`() {
        val transferAmount = 100.0f
        val response = createP2PTransfer(wallet1.cvu, wallet2.cvu, transferAmount)

        assertTransactionBasics(response, TransactionType.P2P, transferAmount)
        assertEquals(wallet1.cvu, response.payerCvu)
        assertEquals(wallet2.cvu, response.payeeCvu)

        assertWalletBalance(wallet1.cvu, initialBalanceWallet1 - transferAmount)
        assertWalletBalance(wallet2.cvu, initialBalanceWallet2 + transferAmount)
    }

    @Test
    fun `createP2PTransfer insufficient funds`() {
        assertThrows<InsufficientFundsException> {
            createP2PTransfer(wallet1.cvu, wallet2.cvu, initialBalanceWallet1 + 100f)
        }
    }

    @Test
    fun `createP2PTransfer payer wallet not found`() {
        assertThrows<WalletNotFoundException> {
            createP2PTransfer(999L, wallet2.cvu, 100.0f)
        }
    }

    @Test
    fun `createP2PTransfer payee wallet not found`() {
        assertThrows<WalletNotFoundException> {
            createP2PTransfer(wallet1.cvu, 999L, 100.0f)
        }
    }

    @Test
    fun `createP2PTransfer same CVU throws InvalidTransactionException`() {
        assertThrows<InvalidTransactionException> {
            createP2PTransfer(wallet1.cvu, wallet1.cvu, 100.0f)
        }
    }

    @Test
    fun `createP2PTransfer negative amount throws InvalidTransactionException`() {
        assertThrows<InvalidTransactionException> {
            createP2PTransfer(wallet1.cvu, wallet2.cvu, -100.0f)
        }
    }
} 