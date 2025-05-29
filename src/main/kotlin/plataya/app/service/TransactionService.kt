package plataya.app.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plataya.app.model.dtos.*
import plataya.app.model.entities.*
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
import plataya.app.repository.TransactionRepository
import plataya.app.repository.WalletRepository
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {

    @Transactional
    fun createP2PTransfer(request: P2PTransferDTO): TransactionResponse {
        if (request.payerCvu == request.payeeCvu) {
            throw InvalidTransactionException("Payer and payee CVU cannot be the same.")
        }
        if (request.amount <= 0.0f) {
            throw InvalidTransactionException("Transaction amount must be positive.")
        }

        val payerWallet = walletRepository.findByCvu(request.payerCvu)
            ?: throw WalletNotFoundException("Payer wallet with CVU ${request.payerCvu} not found.")
        val payeeWallet = walletRepository.findByCvu(request.payeeCvu)
            ?: throw WalletNotFoundException("Payee wallet with CVU ${request.payeeCvu} not found.")

        if (payerWallet.balance < request.amount) {
            throw InsufficientFundsException("Payer wallet has insufficient funds.")
        }

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

         */
    }

    @Transactional
    fun createDeposit(request: DepositDTO): TransactionResponse {
        if (request.amount <= 0.0f) {
            throw InvalidTransactionException("Deposit amount must be positive.")
        }

        val payeeWallet = walletRepository.findByCvu(request.payeeCvu)
            ?: throw WalletNotFoundException("Payee wallet with CVU ${request.payeeCvu} not found.")

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
        if (request.amount <= 0.0f) {
            throw InvalidTransactionException("Withdrawal amount must be positive.")
        }

        val payerWallet = walletRepository.findByCvu(request.payerCvu)
            ?: throw WalletNotFoundException("Payer wallet with CVU ${request.payerCvu} not found.")

        if (payerWallet.balance < request.amount) {
            throw InsufficientFundsException("Payer wallet has insufficient funds for withdrawal.")
        }

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