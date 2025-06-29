package plataya.app.transaction

import plataya.app.client.ExternalWalletClientInterface
import plataya.app.model.dtos.externalwallet.ExternalWalletValidationDTO
import plataya.app.model.dtos.externalwallet.ExternalCvuValidationDTO
import plataya.app.model.dtos.transaction.ExternalDepositResponse
import plataya.app.model.entities.transaction.Currency

class MockExternalWalletClient : ExternalWalletClientInterface {
    
    // Storage for configured responses
    private val cvuValidationResponses = mutableMapOf<Long, ExternalCvuValidationDTO>()
    private val balanceValidationResponses = mutableMapOf<Pair<Long, Float>, ExternalWalletValidationDTO>()
    private val depositResponses = mutableMapOf<DepositRequest, ExternalDepositResponse>()
    
    // Data class to hold deposit request parameters
    data class DepositRequest(
        val destinationCvu: Long,
        val amount: Float,
        val currency: Currency,
        val reference: String
    )
    
    // Methods to configure responses for testing
    fun configureCvuValidation(cvu: Long, response: ExternalCvuValidationDTO) {
        cvuValidationResponses[cvu] = response
    }
    
    fun configureBalanceValidation(cvu: Long, amount: Float, response: ExternalWalletValidationDTO) {
        balanceValidationResponses[Pair(cvu, amount)] = response
    }
    
    fun configureDepositResponse(destinationCvu: Long, amount: Float, currency: Currency, reference: String, response: ExternalDepositResponse) {
        depositResponses[DepositRequest(destinationCvu, amount, currency, reference)] = response
    }
    
    // Clear all configured responses (useful for test cleanup)
    fun clearAll() {
        cvuValidationResponses.clear()
        balanceValidationResponses.clear()
        depositResponses.clear()
        shouldThrowCvuException = null
        shouldThrowBalanceException = null
        shouldThrowDepositException = null
    }
    
    // Storage for exception configuration
    private var shouldThrowCvuException: Exception? = null
    private var shouldThrowBalanceException: Exception? = null
    private var shouldThrowDepositException: Exception? = null
    
    // Methods to configure exceptions for testing
    fun configureCvuException(exception: Exception) {
        shouldThrowCvuException = exception
    }
    
    fun configureBalanceException(exception: Exception) {
        shouldThrowBalanceException = exception
    }
    
    fun configureDepositException(exception: Exception) {
        shouldThrowDepositException = exception
    }

    // Interface implementations
    override fun validateExternalCvu(cvu: Long): ExternalCvuValidationDTO {
        shouldThrowCvuException?.let { throw it }
        return cvuValidationResponses[cvu] 
            ?: throw RuntimeException("No CVU validation configured for CVU: $cvu")
    }
    
    override fun validateExternalBalance(cvu: Long, amount: Float): ExternalWalletValidationDTO {
        shouldThrowBalanceException?.let { throw it }
        return balanceValidationResponses[Pair(cvu, amount)]
            ?: throw RuntimeException("No balance validation configured for CVU: $cvu, amount: $amount")
    }
    
    override fun makeExternalDeposit(destinationCvu: Long, amount: Float, currency: Currency, reference: String): ExternalDepositResponse {
        shouldThrowDepositException?.let { throw it }
        val request = DepositRequest(destinationCvu, amount, currency, reference)
        return depositResponses[request]
            ?: throw RuntimeException("No deposit response configured for: $request")
    }
} 