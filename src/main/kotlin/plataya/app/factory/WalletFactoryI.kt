package plataya.app.factory

import plataya.app.dto.WalletDTO
import plataya.app.entity.Wallet

interface WalletFactoryI {
    fun createWalletDTO(mail: String): WalletDTO
    fun createWalletEntity(wallet: WalletDTO): Wallet
}
