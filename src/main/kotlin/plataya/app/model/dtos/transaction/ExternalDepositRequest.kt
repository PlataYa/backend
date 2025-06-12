package plataya.app.model.dtos.transaction

import plataya.app.model.entities.transaction.Currency

data class ExternalDepositRequest(
    val destinationCvu: Long,
    val amount: Float,
    val currency: Currency,
    val reference: String
)

data class ExternalDepositResponse(
    val transactionId: String,
    val destinationCvu: Long,
    val amount: Float,
    val currency: Currency,
    val status: String, // "COMPLETED", "FAILED", etc.
    val message: String? = null
) 