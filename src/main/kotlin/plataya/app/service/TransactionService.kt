package plataya.app.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.TransactionNotFoundException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.dtos.DepositDTO
import plataya.app.model.dtos.P2PTransferDTO
import plataya.app.model.dtos.TransactionResponse
import plataya.app.model.dtos.WithdrawalDTO
import plataya.app.model.entities.Transaction
import plataya.app.model.entities.TransactionStatus
import plataya.app.model.entities.TransactionType
import plataya.app.model.entities.Wallet
import plataya.app.repository.TransactionRepository
import plataya.app.repository.WalletRepository
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
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
        validatePositiveAmount(request.amount, "Transaction")

        if (request.payerCvu == request.payeeCvu) {
            throw InvalidTransactionException("Payer and payee CVU cannot be the same.")
        }

        val payerWallet = findWalletOrThrow(request.payerCvu, "Payer")
        val payeeWallet = findWalletOrThrow(request.payeeCvu, "Payee")

        validateSufficientFunds(payerWallet, request.amount)

        val updatedPayerWallet = payerWallet.copy(balance = payerWallet.balance - request.amount)
        walletRepository.save(updatedPayerWallet)
        val updatedPayeeWallet = payeeWallet.copy(balance = payeeWallet.balance + request.amount)
        walletRepository.save(updatedPayeeWallet)

        val transaction = Transaction(
            type = TransactionType.P2P,
            payerWallet = payerWallet,
            payeeWallet = payeeWallet,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.COMPLETED,
            createdAt = LocalDateTime.now()
        )

        val savedTransaction = transactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }

    @Transactional
    fun createDeposit(request: DepositDTO): TransactionResponse {
        validatePositiveAmount(request.amount, "Deposit")

        val payeeWallet = findWalletOrThrow(request.payeeCvu, "Payee")

        val updatedPayeeWalletForDeposit = payeeWallet.copy(balance = payeeWallet.balance + request.amount)
        walletRepository.save(updatedPayeeWalletForDeposit)

        val transaction = Transaction(
            type = TransactionType.DEPOSIT,
            payeeWallet = payeeWallet,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.COMPLETED,
            externalReference = request.externalReference,
            createdAt = LocalDateTime.now()
        )
        val savedTransaction = transactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }

    @Transactional
    fun createWithdrawal(request: WithdrawalDTO): TransactionResponse {
        validatePositiveAmount(request.amount, "Withdrawal")

        val payerWallet = findWalletOrThrow(request.payerCvu, "Payer")

        validateSufficientFunds(payerWallet, request.amount)

        val updatedPayerWalletForWithdrawal = payerWallet.copy(balance = payerWallet.balance - request.amount)
        walletRepository.save(updatedPayerWalletForWithdrawal)

        val transaction = Transaction(
            type = TransactionType.WITHDRAWAL,
            payerWallet = payerWallet,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.COMPLETED,
            externalReference = request.externalReference,
            createdAt = LocalDateTime.now()
        )
        val savedTransaction = transactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }

    fun getTransactionById(transactionId: Long): TransactionResponse {
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { TransactionNotFoundException("Transaction with ID $transactionId not found.") }
        return transaction.toResponse()
    }

    fun getTransactionsByCvu(cvu: Long): List<TransactionResponse> {
        walletRepository.findById(cvu)
            .orElseThrow { WalletNotFoundException("Wallet with CVU $cvu not found.") }
        
        val transactions = transactionRepository.findAllByCvu(cvu)
        return transactions.map { it.toResponse() }
    }

    private fun Transaction.toResponse(): TransactionResponse {
        return TransactionResponse(
            transactionId = this.transactionId!!,
            type = this.type,
            payerCvu = this.payerWallet?.cvu,
            payeeCvu = this.payeeWallet?.cvu,
            amount = this.amount,
            currency = this.currency,
            status = this.status,
            createdAt = this.createdAt,
            externalReference = this.externalReference
        )
    }
} 