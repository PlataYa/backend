package plataya.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plataya.app.model.dtos.CvuValidationResponseDTO
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.dtos.AllWalletsDTO
import plataya.app.model.dtos.BalanceDTO
import plataya.app.model.dtos.WalletDTO
import plataya.app.service.WalletService

@RestController
@RequestMapping("/api/v1/wallet")
class WalletController @Autowired constructor(
    private val walletService: WalletService
) {
    @GetMapping("/{cvu}")
    fun getWalletByCvu(@PathVariable cvu: Long): ResponseEntity<WalletDTO> {
        val wallet = walletService.getWalletByCvu(cvu)
        return ResponseEntity.ok(wallet)
    }

    @GetMapping("/all")
    fun getAllWallets(): ResponseEntity<AllWalletsDTO> {
        val wallets = walletService.getAllWallets()
        return ResponseEntity.ok(wallets)
    }

    @GetMapping("/valid/cvu")
    fun validateCvu(@RequestParam cvu: Long): ResponseEntity<CvuValidationResponseDTO> {
        val exists = walletService.validateCvu(cvu)
        return ResponseEntity.ok(exists)
    }

    @GetMapping("/balance/{cvu}")
    fun getBalanceByCvu(@PathVariable cvu: Long): ResponseEntity<BalanceDTO> {
        val balance = walletService.getBalanceByCvu(cvu)
        return ResponseEntity.ok(balance)
    }

    @ExceptionHandler(WalletNotFoundException::class)
    fun handleWalletNotFound(ex: WalletNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message ?: "Wallet not found")
    }
}
