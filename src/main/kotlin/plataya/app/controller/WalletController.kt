package plataya.app.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/wallet")
class WalletController {
    @PostMapping("/create")
    fun createWallet(): String {
        // This is a placeholder for the actual wallet creation logic
        return "Wallet created successfully"
    }
}
