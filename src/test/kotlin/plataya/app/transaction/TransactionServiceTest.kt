package plataya.app.transaction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import plataya.app.model.dtos.P2PTransferDTO
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.TransactionNotFoundException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.dtos.DepositDTO
import plataya.app.model.dtos.WithdrawalDTO
import plataya.app.model.entities.*
import plataya.app.service.TransactionService
import plataya.app.wallet.MockWalletRepository
import java.util.*

class TransactionServiceTest {
    private lateinit var mockWalletRepository: MockWalletRepository
    private lateinit var mockTransactionRepository: MockTransactionRepository
    private lateinit var transactionService: TransactionService

    private lateinit var mockUser1: User
    private lateinit var mockUser2: User
    private lateinit var wallet1: Wallet
    private lateinit var wallet2: Wallet

    private val initialBalanceWallet1 = 1000.0f
    private val initialBalanceWallet2 = 500.0f

    @BeforeEach
    fun setUp() {
        mockWalletRepository = MockWalletRepository()
        mockTransactionRepository = MockTransactionRepository()
        transactionService = TransactionService(mockTransactionRepository, mockWalletRepository)

        mockUser1 = User(
            id = UUID.randomUUID(), mail = "user1@mail.com", password = "pass", name = "User", lastname = "One",
            dayOfBirth = ""
        )
        mockUser2 = User(
            id = UUID.randomUUID(), mail = "user2@mail.com", password = "pass", name = "User", lastname = "Two",
            dayOfBirth = ""
        )

        wallet1 = Wallet(cvu = 111L, user = mockUser1, balance = initialBalanceWallet1)
        wallet2 = Wallet(cvu = 222L, user = mockUser2, balance = initialBalanceWallet2)

        mockWalletRepository.save(wallet1)
        mockWalletRepository.save(wallet2)
    }

    @Test
    fun `createP2PTransfer success`() {
        val transferAmount = 100.0f
        val request = P2PTransferDTO(payerCvu = wallet1.cvu, payeeCvu = wallet2.cvu, amount = transferAmount)

        val response = transactionService.createP2PTransfer(request)

        assertEquals(TransactionType.P2P, response.type)
        assertEquals(transferAmount, response.amount)
        assertEquals(TransactionStatus.COMPLETED, response.status)
        assertEquals(wallet1.cvu, response.payerCvu)
        assertEquals(wallet2.cvu, response.payeeCvu)

        val finalPayerWalletOptional = mockWalletRepository.findById(wallet1.cvu)
        val finalPayeeWalletOptional = mockWalletRepository.findById(wallet2.cvu)

        assertTrue(finalPayerWalletOptional.isPresent, "Payer wallet should be present")
        assertTrue(finalPayeeWalletOptional.isPresent, "Payee wallet should be present")

        val finalPayerWallet = finalPayerWalletOptional.get()
        val finalPayeeWallet = finalPayeeWalletOptional.get()

        assertEquals(initialBalanceWallet1 - transferAmount, finalPayerWallet.balance, 0.001f)
        assertEquals(initialBalanceWallet2 + transferAmount, finalPayeeWallet.balance, 0.001f)

        val savedTransactionOptional = mockTransactionRepository.findById(response.transactionId)
        assertTrue(savedTransactionOptional.isPresent, "Saved transaction should be present")
        val savedTransaction = savedTransactionOptional.get()
        assertEquals(transferAmount, savedTransaction.amount)
    }

    @Test
    fun `createDeposit success`() {
        val depositAmount = 200.0f
        val request = DepositDTO(payeeCvu = wallet1.cvu, amount = depositAmount, externalReference = "ref123")

        val response = transactionService.createDeposit(request)

        assertEquals(TransactionType.DEPOSIT, response.type)
        assertEquals(depositAmount, response.amount)
        assertEquals(TransactionStatus.COMPLETED, response.status)
        assertEquals(wallet1.cvu, response.payeeCvu)
        assertNull(response.payerCvu)
        assertEquals("ref123", response.externalReference)

        val finalWalletOptional = mockWalletRepository.findById(wallet1.cvu)
        assertTrue(finalWalletOptional.isPresent)
        assertEquals(initialBalanceWallet1 + depositAmount, finalWalletOptional.get().balance, 0.001f)

        val savedTransactionOptional = mockTransactionRepository.findById(response.transactionId)
        assertTrue(savedTransactionOptional.isPresent)
        assertEquals(depositAmount, savedTransactionOptional.get().amount)
    }

    @Test
    fun `createWithdrawal success`() {
        val withdrawalAmount = 100.0f
        val request = WithdrawalDTO(payerCvu = wallet1.cvu, amount = withdrawalAmount, externalReference = "ref456")

        val response = transactionService.createWithdrawal(request)

        assertEquals(TransactionType.WITHDRAWAL, response.type)
        assertEquals(withdrawalAmount, response.amount)
        assertEquals(TransactionStatus.COMPLETED, response.status)
        assertEquals(wallet1.cvu, response.payerCvu)
        assertNull(response.payeeCvu)
        assertEquals("ref456", response.externalReference)

        val finalWalletOptional = mockWalletRepository.findById(wallet1.cvu)
        assertTrue(finalWalletOptional.isPresent)
        assertEquals(initialBalanceWallet1 - withdrawalAmount, finalWalletOptional.get().balance, 0.001f)

        val savedTransactionOptional = mockTransactionRepository.findById(response.transactionId)
        assertTrue(savedTransactionOptional.isPresent)
        assertEquals(withdrawalAmount, savedTransactionOptional.get().amount)
    }

    @Test
    fun `getTransactionById success`() {
        val transferAmount = 50.0f
        val p2pRequest = P2PTransferDTO(payerCvu = wallet1.cvu, payeeCvu = wallet2.cvu, amount = transferAmount)
        val createdTransactionResponse = transactionService.createP2PTransfer(p2pRequest)

        val foundTransactionResponse = transactionService.getTransactionById(createdTransactionResponse.transactionId)

        assertNotNull(foundTransactionResponse)
        assertEquals(createdTransactionResponse.transactionId, foundTransactionResponse.transactionId)
        assertEquals(TransactionType.P2P, foundTransactionResponse.type)
        assertEquals(transferAmount, foundTransactionResponse.amount)
        assertEquals(wallet1.cvu, foundTransactionResponse.payerCvu)
        assertEquals(wallet2.cvu, foundTransactionResponse.payeeCvu)
    }
    
    @Test
    fun `Create transfer insufficient funds`() {
        val request = P2PTransferDTO(payerCvu = wallet1.cvu, payeeCvu = wallet2.cvu, amount = initialBalanceWallet1 + 100f)

        assertThrows<InsufficientFundsException> {
            transactionService.createP2PTransfer(request)
        }
    }

    @Test
    fun `Create transfer payer wallet not found`() {
        val request = P2PTransferDTO(payerCvu = 999L, payeeCvu = wallet2.cvu, amount = 100.0f)

        assertThrows<WalletNotFoundException> {
            transactionService.createP2PTransfer(request)
        }
    }

    @Test
    fun `Create transfer payee wallet not found`() {
        val request = P2PTransferDTO(payerCvu = wallet1.cvu, payeeCvu = 999L, amount = 100.0f)

        assertThrows<WalletNotFoundException> {
            transactionService.createP2PTransfer(request)
        }
    }

    @Test
    fun `Create transfer same CVU throws InvalidTransactionException`() {
        val request = P2PTransferDTO(payerCvu = wallet1.cvu, payeeCvu = wallet1.cvu, amount = 100.0f)
        assertThrows<InvalidTransactionException> {
            transactionService.createP2PTransfer(request)
        }
    }

    @Test
    fun `Create transfer negative amount throws InvalidTransactionException`() {
        val request = P2PTransferDTO(payerCvu = wallet1.cvu, payeeCvu = wallet2.cvu, amount = -100.0f)
        assertThrows<InvalidTransactionException> {
            transactionService.createP2PTransfer(request)
        }
    }

    @Test
    fun `getTransactionById not found`() {
        assertThrows<TransactionNotFoundException> {
            transactionService.getTransactionById(999L)
        }
    }
}