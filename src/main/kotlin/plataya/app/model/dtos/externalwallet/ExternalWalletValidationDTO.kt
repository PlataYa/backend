package plataya.app.model.dtos.externalwallet

data class ExternalWalletValidationDTO(
    val cvu: Long,
    val exists: Boolean,
    val balance: Float? = null,
    val hasSufficientFunds: Boolean = false
) 