package plataya.app.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import plataya.app.dto.WalletDTO
import plataya.app.factory.WalletFactory

@Service
class WalletService(
    @Autowired private val walletFactory: WalletFactory
) {

    fun createWallet(mail: String): WalletDTO {
        return walletFactory.createWallet(mail)
    }
}
