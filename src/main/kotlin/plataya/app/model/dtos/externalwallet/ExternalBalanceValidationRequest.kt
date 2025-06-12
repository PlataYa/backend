package plataya.app.model.dtos.externalwallet

data class ExternalBalanceValidationRequest(
    val cvu: Long,
    val amount: Float
) 