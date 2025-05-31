package plataya.app.factory

import org.springframework.stereotype.Component
import plataya.app.model.dtos.WalletDTO
import plataya.app.model.entities.User
import plataya.app.model.entities.Wallet
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
