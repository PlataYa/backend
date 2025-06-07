package plataya.app.model.dtos

data class ExternalWalletValidationDTO(
    val cvu: Long,
    val exists: Boolean,
    val balance: Float? = null,
    val hasSufficientFunds: Boolean = false
) 