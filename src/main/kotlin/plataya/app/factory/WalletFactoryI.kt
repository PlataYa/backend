package plataya.app.factory

import plataya.app.model.dtos.WalletDTO
import plataya.app.model.entities.User
import plataya.app.model.entities.Wallet

interface WalletFactoryI {
    fun translateWalletEntityToDTO(wallet: Wallet): WalletDTO
    fun createWalletEntity(user: User): Wallet
}
