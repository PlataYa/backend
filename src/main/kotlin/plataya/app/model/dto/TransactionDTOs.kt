package plataya.app.model.dto

import plataya.app.model.entity.TransactionStatus
import plataya.app.model.entity.TransactionType
import plataya.app.model.entity.Currency
import java.time.LocalDateTime

// Request DTOs
data class P2PTransferDTO(
    val payerCvu: Long,
    val payeeCvu: Long,
    val amount: Float,
    val currency: Currency = Currency.ARS
)

data class DepositDTO(
    val payeeCvu: Long,
    val amount: Float,
    val currency: Currency = Currency.ARS,
    val externalReference: String
)

data class WithdrawalDTO(
    val payerCvu: Long,
    val amount: Float,
    val currency: Currency = Currency.ARS,
    val externalReference: String
)

// Response DTO
data class TransactionResponse(
    val transactionId: Long,
    val type: TransactionType,
    val payerCvu: Long?,
    val payeeCvu: Long?,
    val amount: Float,
    val currency: Currency,
    val status: TransactionStatus,
    val createdAt: LocalDateTime,
    val externalReference: String?
) 