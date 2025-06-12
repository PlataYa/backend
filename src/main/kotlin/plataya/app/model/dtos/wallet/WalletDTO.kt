package plataya.app.model.dtos.wallet

data class WalletDTO(
    val userMail: String,
    val cvu: Long,
    val balance: Float
)