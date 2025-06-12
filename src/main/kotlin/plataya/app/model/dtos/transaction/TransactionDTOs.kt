package plataya.app.model.dtos.transaction

import plataya.app.model.entities.transaction.Currency


// Request DTOs
data class P2PTransferDTO(
    val payerCvu: Long,
    val payeeCvu: Long,
    val amount: Float,
    val currency: Currency = Currency.ARS
)

data class ExternalTransactionDTO(
    // Both cvus depends on the type of external transaction
    val sourceCvu: Long,
    val destinationCvu: Long,
    val amount: Float,
    val currency: Currency = Currency.ARS,
    val externalReference: String?
)
