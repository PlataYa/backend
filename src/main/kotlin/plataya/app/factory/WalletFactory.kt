package plataya.app.factory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import plataya.app.dto.WalletDTO
import plataya.app.service.PlataYaCVUCounter

@Component
class WalletFactory {
    private val walletCount = PlataYaCVUCounter()

    fun createWallet(mail: String): WalletDTO {
        return WalletDTO(mail, walletCount.getNextCVU(), 0F)
    }
}
