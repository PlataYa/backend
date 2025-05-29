package plataya.app.service

import org.springframework.stereotype.Service
import plataya.app.dto.WalletDTO

@Service
class WalletService {
    private var walletCount = PlataYaCVUCounter()

    fun createWallet(mail: String): WalletDTO {
        return WalletDTO(mail, walletCount.getNextCVU(), 0F)
    }
}
