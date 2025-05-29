package plataya.app.dto

import plataya.app.entity.TransactionStatus
import plataya.app.entity.TransactionType
import plataya.app.entity.Currency
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