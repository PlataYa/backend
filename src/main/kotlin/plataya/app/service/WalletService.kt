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

    fun getWalletByCvu(cvu: Long): WalletDTO {
        val wallet = walletRepository.findById(cvu)
        if (wallet.isEmpty) {
            throw NoSuchElementException("Wallet with CVU $cvu not found")
        }
        return walletFactory.translateWalletEntityToDTO(wallet.get())
    }

    fun updateBalance(cvu: Long, transferenceValue: Float): WalletDTO {
        val wallet = walletRepository.findById(cvu)
        if (wallet.isEmpty) {
            throw NoSuchElementException("Wallet with CVU $cvu not found")
        }
        val currentWallet = wallet.get()

        val newBalance = currentWallet.balance + transferenceValue
        if (newBalance < 0) {
            throw IllegalArgumentException("Insufficient balance for the transaction")
        }

        val updatedWalletToSave = currentWallet.copy(balance = newBalance)
        val updatedWallet = walletRepository.save(updatedWalletToSave)
        val updatedWalletDTO = walletFactory.translateWalletEntityToDTO(updatedWallet)

        return updatedWalletDTO
    }
}
