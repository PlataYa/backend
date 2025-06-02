package plataya.app.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.dtos.WalletDTO
import plataya.app.factory.WalletFactory
import plataya.app.model.dtos.AllWalletsDTO
import plataya.app.model.dtos.BalanceDTO
import plataya.app.model.dtos.CvuValidationResponseDTO
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

    fun getAllWallets(): AllWalletsDTO {
        val wallets = walletRepository.findAll()
        val walletDTOs = wallets.map { walletFactory.translateWalletEntityToDTO(it) }
        return AllWalletsDTO(wallets = walletDTOs)
    }

    fun validateCvu(cvu: Long): CvuValidationResponseDTO {
        val exists = walletRepository.existsByCvu(cvu)
        return CvuValidationResponseDTO(valid = exists)
    }

    fun getWalletByCvu(cvu: Long): WalletDTO {
        val wallet = walletRepository.findById(cvu)
        if (wallet.isEmpty) {
            throw WalletNotFoundException("Wallet with CVU $cvu not found")
        }
        return walletFactory.translateWalletEntityToDTO(wallet.get())
    }

    fun getBalanceByCvu(cvu: Long): BalanceDTO {
        val wallet = getWalletByCvu(cvu)
        return BalanceDTO(cvu= cvu, balance = wallet.balance)
    }

    fun updateBalance(cvu: Long, transferenceValue: Float): WalletDTO {
        val wallet = walletRepository.findById(cvu)
        if (wallet.isEmpty) {
            throw WalletNotFoundException("Wallet with CVU $cvu not found")
        }
        val currentWallet = wallet.get()

        val newBalance = currentWallet.balance + transferenceValue
        if (newBalance < 0) {
            throw InsufficientFundsException("Insufficient balance for the transaction")
        }

        val updatedWalletToSave = currentWallet.copy(balance = newBalance)
        val updatedWallet = walletRepository.save(updatedWalletToSave)
        val updatedWalletDTO = walletFactory.translateWalletEntityToDTO(updatedWallet)

        return updatedWalletDTO
    }
}
