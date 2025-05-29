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
        val exists = walletService.validateCvu(cvu)
        return ResponseEntity.ok(CvuValidationResponseDTO(valid = exists))
    }
}
