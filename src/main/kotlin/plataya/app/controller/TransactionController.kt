package plataya.app.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.InvalidTransactionException
import plataya.app.exception.TransactionNotFoundException
import plataya.app.exception.WalletNotFoundException
import plataya.app.model.dto.DepositDTO
import plataya.app.model.dto.P2PTransferDTO
import plataya.app.model.dto.TransactionResponse
import plataya.app.model.dto.WithdrawalDTO
import plataya.app.service.TransactionService

@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/transfer")
    fun createP2PTransfer(@RequestBody request: P2PTransferDTO): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.createP2PTransfer(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/deposit")
    fun createDeposit(@RequestBody request: DepositDTO): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.createDeposit(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/withdraw")
    fun createWithdrawal(@RequestBody request: WithdrawalDTO): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.createWithdrawal(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @GetMapping("/{id}")
    fun getTransactionById(@PathVariable id: Long): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.getTransactionById(id)
        return ResponseEntity.ok(transaction)
    }
    
    @ExceptionHandler(WalletNotFoundException::class)
    fun handleWalletNotFound(ex: WalletNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }

    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFunds(ex: InsufficientFundsException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
    }

    @ExceptionHandler(InvalidTransactionException::class)
    fun handleInvalidTransaction(ex: InvalidTransactionException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
    }

    @ExceptionHandler(TransactionNotFoundException::class)
    fun handleTransactionNotFound(ex: TransactionNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }
} 