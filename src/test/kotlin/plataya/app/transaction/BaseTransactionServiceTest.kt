package plataya.app.transaction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import plataya.app.model.dtos.*
import plataya.app.model.entities.*
import plataya.app.service.TransactionService
import plataya.app.wallet.MockWalletRepository
import java.util.*

abstract class BaseTransactionServiceTest {
    protected lateinit var mockWalletRepository: MockWalletRepository
    protected lateinit var mockTransactionRepository: MockTransactionRepository
    protected lateinit var transactionService: TransactionService

    protected lateinit var mockUser1: User
    protected lateinit var mockUser2: User
    protected lateinit var wallet1: Wallet
    protected lateinit var wallet2: Wallet

    protected val initialBalanceWallet1 = 1000.0f
    protected val initialBalanceWallet2 = 500.0f

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

    // Common transaction creation methods
    protected fun createP2PTransfer(payerCvu: Long, payeeCvu: Long, amount: Float): TransactionResponse {
        val request = P2PTransferDTO(payerCvu = payerCvu, payeeCvu = payeeCvu, amount = amount)
        return transactionService.createP2PTransfer(request)
    }

    protected fun createDeposit(payeeCvu: Long, amount: Float, externalRef: String): TransactionResponse {
        val request = DepositDTO(payeeCvu = payeeCvu, amount = amount, externalReference = externalRef)
        return transactionService.createDeposit(request)
    }

    protected fun createWithdrawal(payerCvu: Long, amount: Float, externalRef: String): TransactionResponse {
        val request = WithdrawalDTO(payerCvu = payerCvu, amount = amount, externalReference = externalRef)
        return transactionService.createWithdrawal(request)
    }

    // Common assertion methods
    protected fun assertTransactionBasics(
        response: TransactionResponse,
        expectedType: TransactionType,
        expectedAmount: Float,
        expectedStatus: TransactionStatus = TransactionStatus.COMPLETED
    ) {
        assertEquals(expectedType, response.type)
        assertEquals(expectedAmount, response.amount)
        assertEquals(expectedStatus, response.status)
        assertNotNull(response.transactionId)
        assertNotNull(response.createdAt)
    }

    protected fun assertTransactionSaved(transactionId: Long, expectedAmount: Float) {
        val savedTransactionOptional = mockTransactionRepository.findById(transactionId)
        assertTrue(savedTransactionOptional.isPresent, "Saved transaction should be present")
        assertEquals(expectedAmount, savedTransactionOptional.get().amount)
    }

    protected fun assertWalletBalance(cvu: Long, expectedBalance: Float, tolerance: Float = 0.001f) {
        val walletOptional = mockWalletRepository.findById(cvu)
        assertTrue(walletOptional.isPresent, "Wallet should exist")
        assertEquals(expectedBalance, walletOptional.get().balance, tolerance)
    }
} 