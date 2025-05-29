package plataya.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plataya.app.model.dtos.CvuValidationResponseDTO
import plataya.app.repository.WalletRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import plataya.app.dto.CreateWalletRequest
import plataya.app.service.WalletService

@RestController
@RequestMapping("/api/v1/wallet")
class WalletController @Autowired constructor(
    private val walletService: WalletService
) {
    @PostMapping("/create")
    fun createWallet(@RequestBody createWalletRequest: CreateWalletRequest): ResponseEntity<Any> {
//        Should validate that the mail is associated with a registered user
        val userMail = createWalletRequest.userMail

        if (userMail.isBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "User mail cannot be empty"))
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(userMail)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid email format"))
        }

        return try {
            val wallet = walletService.createWallet(userMail)
            ResponseEntity.ok(wallet)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/all")
    fun getAllWallets(): ResponseEntity<Any> {
        return try {
            val wallets = walletService.getAllWallets()
            ResponseEntity.ok(wallets)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/valid/cvu")
    fun validateCvu(@RequestParam cvu: Long): ResponseEntity<CvuValidationResponseDTO> {
        val exists = walletRepository.existsByCvu(cvu)
        return ResponseEntity.ok(CvuValidationResponseDTO(valid = exists))
    }
}
