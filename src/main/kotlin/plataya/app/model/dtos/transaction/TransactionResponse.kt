package plataya.app.model.dtos.transaction

import plataya.app.model.entities.transaction.TransactionStatus
import plataya.app.model.entities.transaction.TransactionType
import plataya.app.model.entities.transaction.Currency

import java.time.LocalDateTime

// Base interface for all transaction responses
data class TransactionResponse (
    val transactionId: Long,
    val type: TransactionType,
    val amount: Float,
    val currency: Currency,
    val status: TransactionStatus,
    val createdAt: LocalDateTime,
    val payerCvu: Long,
    val payeeCvu: Long,
)

// External Transaction Response - unified for both deposits and withdrawals
data class ExternalTransactionResponse(
    val transactionId: Long,
    val type: TransactionType,
    val sourceCvu: Long,
    val destinationCvu: Long,
    val externalReference: String,
    val amount: Float,
    val currency: Currency,
    val status: TransactionStatus,
    val createdAt: LocalDateTime
)