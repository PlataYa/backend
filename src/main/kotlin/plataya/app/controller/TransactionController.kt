package plataya.app.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plataya.app.exception.*
import plataya.app.model.dtos.transaction.ExternalTransactionDTO
import plataya.app.model.dtos.transaction.ExternalTransactionResponse
import plataya.app.model.dtos.transaction.P2PTransferDTO
import plataya.app.model.dtos.transaction.TransactionResponse
import plataya.app.service.TransactionService

@RestController
@RequestMapping("/api/v1/transaction")
class TransactionController(
    private val transactionService: TransactionService
) {
    // P2P Transfer endpoints
    @PostMapping("/transfer")
    fun createP2PTransfer(@RequestBody request: P2PTransferDTO): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.createP2PTransfer(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @GetMapping("/transfer/{id}")
    fun getTransactionById(@PathVariable id: Long): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.getTransactionById(id)
        return ResponseEntity.ok(transaction)
    }

    @PostMapping("/deposit")
    fun createDeposit(@RequestBody request: ExternalTransactionDTO): ResponseEntity<ExternalTransactionResponse> {
        val transaction = transactionService.createDeposit(request)
        println("Transaction: $transaction")
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/withdrawal")
    fun createWithdrawal(@RequestBody request: ExternalTransactionDTO): ResponseEntity<ExternalTransactionResponse> {
        val transaction = transactionService.createWithdrawal(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @GetMapping("/{cvu}/history")
    fun getWalletTransactionHistory(@PathVariable cvu: Long): ResponseEntity<List<TransactionResponse>> {
        val transactions = transactionService.getWalletTransactionHistory(cvu)
        return ResponseEntity.ok(transactions)
    }

    // Exception handlers
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

    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternalServiceError(ex: ExternalServiceException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.message)
    }

    @ExceptionHandler(ExternalWalletNotFoundException::class)
    fun handleExternalWalletNotFound(ex: ExternalWalletNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
    }

    @ExceptionHandler(ExternalInsufficientFundsException::class)
    fun handleExternalInsufficientFunds(ex: ExternalInsufficientFundsException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
    }
} 