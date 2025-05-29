package plataya.app.factory

import org.springframework.stereotype.Component
import plataya.app.dto.WalletDTO
import plataya.app.entity.Wallet
import plataya.app.service.PlataYaCVUCounter

@Component
class WalletFactory: WalletFactoryI {
    private val walletCount = PlataYaCVUCounter()

    override fun createWalletDTO(mail: String): WalletDTO {
        return WalletDTO(mail, walletCount.getNextCVU(), 0F)
    }

    override fun createWalletEntity(wallet: WalletDTO): Wallet {
        return Wallet(
            mail = wallet.mail,
            cvu = wallet.cvu,
            balance = wallet.balance
        )
    }
}
