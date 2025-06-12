package plataya.app.transaction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import plataya.app.model.dtos.transaction.P2PTransferDTO
import plataya.app.model.dtos.transaction.TransactionResponse
import plataya.app.model.dtos.transaction.ExternalTransactionDTO
import plataya.app.model.dtos.transaction.ExternalTransactionResponse
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.entities.transaction.TransactionStatus
import plataya.app.model.entities.transaction.Currency
import plataya.app.model.entities.user.User
import plataya.app.model.entities.wallet.Wallet
import plataya.app.service.TransactionService
import plataya.app.wallet.MockWalletRepository
import java.util.UUID

abstract class BaseTransactionServiceTest {
    protected lateinit var mockWalletRepository: MockWalletRepository
    protected lateinit var mockP2PTransactionRepository: MockP2PTransactionRepository
    protected lateinit var mockExternalTransactionRepository: MockExternalTransactionRepository
    protected lateinit var mockExternalWalletClient: MockExternalWalletClient
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
        mockP2PTransactionRepository = MockP2PTransactionRepository()
        mockExternalTransactionRepository = MockExternalTransactionRepository()
        mockExternalWalletClient = MockExternalWalletClient()
        
        // Clear any stored data from previous tests
        mockP2PTransactionRepository.clearAll()
        mockExternalTransactionRepository.clearAll()
        mockExternalWalletClient.clearAll()
        
        transactionService = TransactionService(
            mockP2PTransactionRepository,
            mockExternalTransactionRepository,
            mockWalletRepository,
            mockExternalWalletClient
        )

        mockUser1 = User(
            id = UUID.randomUUID(), 
            mail = "user1@mail.com", 
            password = "pass", 
            name = "User", 
            lastname = "One",
            dayOfBirth = "1990-01-01"
        )
        mockUser2 = User(
            id = UUID.randomUUID(), 
            mail = "user2@mail.com", 
            password = "pass", 
            name = "User", 
            lastname = "Two",
            dayOfBirth = "1990-01-01"
        )

        wallet1 = Wallet(cvu = 111L, user = mockUser1, balance = initialBalanceWallet1)
        wallet2 = Wallet(cvu = 222L, user = mockUser2, balance = initialBalanceWallet2)

        mockWalletRepository.save(wallet1)
        mockWalletRepository.save(wallet2)
    }

    // Common transaction creation methods
    protected fun createP2PTransfer(payerCvu: Long, payeeCvu: Long, amount: Float): TransactionResponse {
        val request = P2PTransferDTO(
            payerCvu = payerCvu, 
            payeeCvu = payeeCvu, 
            amount = amount,
            currency = Currency.ARS
        )
        return transactionService.createP2PTransfer(request)
    }

    protected fun createDeposit(destinationCvu: Long, amount: Float, sourceCvu: Long = 999L): ExternalTransactionResponse {
        val request = ExternalTransactionDTO(
            sourceCvu = sourceCvu,
            destinationCvu = destinationCvu,
            amount = amount,
            currency = Currency.ARS,
            externalReference = "DEPOSIT_REF"
        )
        return transactionService.createDeposit(request)
    }

    protected fun createWithdrawal(sourceCvu: Long, amount: Float, destinationCvu: Long = 999L): ExternalTransactionResponse {
        val request = ExternalTransactionDTO(
            sourceCvu = sourceCvu,
            destinationCvu = destinationCvu,
            amount = amount,
            currency = Currency.ARS,
            externalReference = "WITHDRAWAL_REF"
        )
        return transactionService.createWithdrawal(request)
    }

    // Common assertion methods for P2P transactions
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

    // Common assertion methods for external transactions
    protected fun assertExternalTransactionBasics(
        response: ExternalTransactionResponse,
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

    protected fun assertWalletBalance(cvu: Long, expectedBalance: Float, tolerance: Float = 0.001f) {
        val walletOptional = mockWalletRepository.findById(cvu)
        assertTrue(walletOptional.isPresent, "Wallet should exist")
        assertEquals(expectedBalance, walletOptional.get().balance, tolerance)
    }
} 