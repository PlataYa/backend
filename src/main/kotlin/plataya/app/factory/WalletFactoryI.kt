package plataya.app.factory

import plataya.app.model.dtos.wallet.WalletDTO
import plataya.app.model.entities.user.User
import plataya.app.model.entities.wallet.Wallet

interface WalletFactoryI {
    fun translateWalletEntityToDTO(wallet: Wallet): WalletDTO
    fun createWalletEntity(user: User): Wallet
}
