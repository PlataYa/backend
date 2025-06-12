package plataya.app.factory

import org.springframework.stereotype.Component
import plataya.app.model.dtos.wallet.WalletDTO
import plataya.app.model.entities.user.User
import plataya.app.model.entities.wallet.Wallet
import plataya.app.service.PlataYaCVUCounter

@Component
class WalletFactory: WalletFactoryI {
    private val walletCount = PlataYaCVUCounter()

    override fun createWalletEntity(user: User): Wallet {
        return Wallet(
            user = user,
            cvu = walletCount.getNextCVU(),
            balance = 0.0F
        )
    }

    override fun translateWalletEntityToDTO(wallet: Wallet): WalletDTO {
        return WalletDTO(
            userMail = wallet.user.mail,
            cvu = wallet.cvu,
            balance = wallet.balance
        )
    }
}
