package plataya.app.factory

import org.springframework.stereotype.Component
import plataya.app.dto.WalletDTO
import plataya.app.entity.Wallet
import plataya.app.service.PlataYaCVUCounter

@Component
class WalletFactory {
    private val walletCount = PlataYaCVUCounter()

    fun createWalletDTO(mail: String): WalletDTO {
        return WalletDTO(mail, walletCount.getNextCVU(), 0F)
    }

    fun createWalletEntity(wallet: WalletDTO): Wallet {
        return Wallet(
            mail = wallet.mail,
            cvu = wallet.cvu,
            balance = wallet.balance
        )
    }
}
