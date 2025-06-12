package plataya.app.model.entities.transaction

import java.time.LocalDateTime

sealed interface TransactionEntity {
    val transactionId: Long?
    val amount: Float
    val currency: Currency
    val status: TransactionStatus
    val createdAt: LocalDateTime
    
    // Helper method to get transaction type
    fun getTransactionType(): TransactionType
} 