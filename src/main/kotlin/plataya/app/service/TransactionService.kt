package plataya.app.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plataya.app.client.ExternalWalletClient
import plataya.app.exception.*
import plataya.app.model.dtos.transaction.ExternalTransactionDTO
import plataya.app.model.dtos.transaction.ExternalTransactionResponse
import plataya.app.model.dtos.transaction.P2PTransferDTO
import plataya.app.model.dtos.transaction.TransactionResponse
import plataya.app.model.entities.transaction.ExternalTransaction
import plataya.app.model.entities.transaction.P2PTransaction
import plataya.app.model.entities.transaction.TransactionStatus
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.entities.transaction.toResponse
import plataya.app.model.entities.transaction.toTransactionResponse
import plataya.app.model.entities.wallet.Wallet
import plataya.app.repository.*
import java.time.LocalDateTime

@Service
class TransactionService(
    private val p2pTransactionRepository: P2PTransactionRepository,
    private val externalTransactionRepository: ExternalTransactionRepository,
    private val walletRepository: WalletRepository,
    private val externalWalletClient: ExternalWalletClient
) {

    private fun validatePositiveAmount(amount: Float, operationName: String) {
        if (amount <= 0.0f) {
            throw InvalidTransactionException("$operationName amount must be positive.")
        }
    }

    private fun findWalletOrThrow(cvu: Long, walletDescription: String): Wallet {
        return walletRepository.findById(cvu)
            .orElseThrow { WalletNotFoundException("$walletDescription wallet with ID $cvu not found.") }
    }

    private fun validateSufficientFunds(wallet: Wallet, amount: Float) {
        if (wallet.balance < amount) {
            throw InsufficientFundsException("${wallet.user.name} ${wallet.user.lastname}'s wallet has insufficient funds.")
        }
    }

    @Transactional
    fun createP2PTransfer(request: P2PTransferDTO): TransactionResponse {
        validatePositiveAmount(request.amount, "P2P Transfer")

        if (request.payerCvu == request.payeeCvu) {
            throw InvalidTransactionException("Payer and payee CVU cannot be the same.")
        }

        val payerWallet = findWalletOrThrow(request.payerCvu, "Payer")
        val payeeWallet = findWalletOrThrow(request.payeeCvu, "Payee")

        validateSufficientFunds(payerWallet, request.amount)

        // Update balances
        val updatedPayerWallet = payerWallet.copy(balance = payerWallet.balance - request.amount)
        walletRepository.save(updatedPayerWallet)
        val updatedPayeeWallet = payeeWallet.copy(balance = payeeWallet.balance + request.amount)
        walletRepository.save(updatedPayeeWallet)

        // Create and save transaction
        val transaction = P2PTransaction(
            payerWallet = payerWallet,
            payeeWallet = payeeWallet,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.COMPLETED,
            createdAt = LocalDateTime.now()
        )

        val savedTransaction = p2pTransactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }

    @Transactional
    fun createDeposit(request: ExternalTransactionDTO): ExternalTransactionResponse {
        validatePositiveAmount(request.amount, "Deposit")

        val internalWallet = findWalletOrThrow(request.destinationCvu, "Internal")
        
        // This is a deposit - money coming into our system
        externalWalletClient.validateExternalBalance(request.sourceCvu, request.amount)
        
        // Update balance - add money to internal wallet
        val updatedWallet = internalWallet.copy(balance = internalWallet.balance + request.amount)
        walletRepository.save(updatedWallet)

        // Create and save transaction
        val transaction = ExternalTransaction(
            internalWallet = internalWallet,
            externalReference = request.externalReference ?: "",
            externalCvu = request.sourceCvu,
            transactionType = TransactionType.DEPOSIT,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.COMPLETED,
            createdAt = LocalDateTime.now()
        )

        val savedTransaction = externalTransactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }

    @Transactional
    fun createWithdrawal(request: ExternalTransactionDTO): ExternalTransactionResponse {
        validatePositiveAmount(request.amount, "Withdrawal")

        val internalWallet = findWalletOrThrow(request.sourceCvu, "Internal")
        
        // This is a withdrawal - money going out of our system
        validateSufficientFunds(internalWallet, request.amount)
        
        // Validate external destination CVU and make deposit
        val externalValidation = externalWalletClient.validateExternalCvu(request.destinationCvu)
        
        // Make the deposit to external API
        val depositResponse = externalWalletClient.makeExternalDeposit(
            destinationCvu = request.destinationCvu,
            amount = request.amount,
            currency = request.currency,
            reference = externalValidation.bankName
        )
        
        // Validate the deposit was successful
        if (depositResponse.status != "COMPLETED") {
            throw InvalidTransactionException("External deposit failed: ${depositResponse.message ?: "Unknown error"}")
        }

        // If external deposit succeeded, reduce balance
        val updatedWallet = internalWallet.copy(balance = internalWallet.balance - request.amount)
        walletRepository.save(updatedWallet)

        // Create and save transaction
        val transaction = ExternalTransaction(
            internalWallet = internalWallet,
            externalReference = externalValidation.bankName,
            externalCvu = request.destinationCvu,
            transactionType = TransactionType.WITHDRAWAL,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.COMPLETED,
            createdAt = LocalDateTime.now()
        )

        val savedTransaction = externalTransactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }

    fun getTransactionById(transactionId: Long): TransactionResponse {
        val p2pTransaction = p2pTransactionRepository.findById(transactionId)
        val externalTransaction = externalTransactionRepository.findById(transactionId)
        if (p2pTransaction.isEmpty) {
            if (externalTransaction.isEmpty) {
                throw  InvalidTransactionException("Transaction with ID $transactionId not found.")
            }
            return externalTransaction.get().toTransactionResponse()
        }
        return p2pTransaction.get().toResponse()
    }

    // Mixed transaction history for a wallet
    fun getWalletTransactionHistory(cvu: Long): List<TransactionResponse> {
        // Verify wallet exists
        walletRepository.findById(cvu)
            .orElseThrow { WalletNotFoundException("Wallet with CVU $cvu not found.") }
        
        // Get all transaction types for this CVU
        val p2pTransactions = p2pTransactionRepository.findAllByCvu(cvu).map { it.toResponse() }
        val externalTransactions = externalTransactionRepository.findAllByCvu(cvu).map { it.toTransactionResponse() }
        
        // Combine and sort by creation time (newest first)
        val allTransactions = (p2pTransactions + externalTransactions)
            .sortedByDescending { it.createdAt }
        
        return allTransactions
    }
} 