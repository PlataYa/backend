package plataya.app.service

import plataya.app.dto.WalletDTO

class WalletService {
    private var walletCount = PlataYaCVUCounter()

    fun createWallet(mail: String): WalletDTO {
        return WalletDTO(mail, walletCount.getNextCVU(), 0F)
    }
}
