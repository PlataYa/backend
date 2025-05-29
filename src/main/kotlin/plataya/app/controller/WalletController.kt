package plataya.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import plataya.app.dto.CreateWalletRequest
import plataya.app.service.WalletService

@RestController
@RequestMapping("/api/v1/wallet")
class WalletController @Autowired constructor(
    private val walletService: WalletService
) {
    @PostMapping("/create")
    fun createWallet(@RequestBody createWalletRequest: CreateWalletRequest): ResponseEntity<Any> {
//        Should validate that the mail is associated with a registered user and if it follows the correct format
        val userMail = createWalletRequest.userMail

        if (userMail.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "User mail cannot be empty"))
        }

        return try {
            val wallet = walletService.createWallet(userMail)
            ResponseEntity.ok(wallet)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to e.message))
        }
    }
}
