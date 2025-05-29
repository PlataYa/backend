package plataya.app.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import plataya.app.dto.WalletDTO
import plataya.app.factory.WalletFactory
import plataya.app.repository.WalletRepository

@Service
class WalletService(
    @Autowired private val walletFactory: WalletFactory,
    @Autowired private val walletRepository: WalletRepository
) {
    fun createWallet(mail: String): WalletDTO {
        val wallet = walletFactory.createWalletDTO(mail)
        val walletEntity = walletFactory.createWalletEntity(wallet)

        walletRepository.save(walletEntity)

        return wallet
    }

    fun getAllWallets(): List<WalletDTO> {
        return walletRepository.findAll().map { wallet ->
            WalletDTO(
                mail = wallet.mail,
                cvu = wallet.cvu,
                balance = wallet.balance
            )
        }
    }
}
