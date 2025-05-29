package plataya.app.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import plataya.app.model.dtos.WalletDTO
import plataya.app.factory.WalletFactory
import plataya.app.model.entities.User
import plataya.app.repository.WalletRepository

@Service
class WalletService(
    @Autowired private val walletFactory: WalletFactory,
    @Autowired private val walletRepository: WalletRepository
) {
    fun createWallet(savedUser: User): WalletDTO {
        val wallet = walletFactory.createWalletEntity(savedUser)

        val savedWallet = walletRepository.save(wallet)

        val walletDTO = walletFactory.translateWalletEntityToDTO(savedWallet)

        return walletDTO
    }

    fun getAllWallets(): List<WalletDTO> {
        return walletRepository.findAll().map { wallet ->
            WalletDTO(
                userMail = wallet.user.mail,
                cvu = wallet.cvu,
                balance = wallet.balance
            )
        }
    }

    fun validateCvu(cvu: Long): Boolean {
        return walletRepository.existsByCvu(cvu)
    }
}
