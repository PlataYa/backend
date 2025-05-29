package plataya.app.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plataya.app.model.dtos.CvuValidationResponseDTO
import plataya.app.repository.WalletRepository

@RestController
@RequestMapping("/api/v1")
class WalletController(private val walletRepository: WalletRepository) {

    @GetMapping("/valid/cvu")
    fun validateCvu(@RequestParam cvu: Long): ResponseEntity<CvuValidationResponseDTO> {
        val exists = walletRepository.existsByCvu(cvu)
        return ResponseEntity.ok(CvuValidationResponseDTO(valid = exists))
    }
}
