package plataya.app.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import plataya.app.model.dtos.ExternalWalletValidationDTO
import plataya.app.model.dtos.ExternalBalanceValidationRequest
import plataya.app.model.dtos.ExternalCvuValidationRequest
import plataya.app.exception.ExternalServiceException
import plataya.app.exception.ExternalWalletNotFoundException
import plataya.app.exception.ExternalInsufficientFundsException

@Service
class ExternalWalletClient(
    private val restTemplate: RestTemplate,
    @Value("\${external.wallet.service.url}") private val externalServiceUrl: String
) {

    fun validateExternalCvu(cvu: Long): ExternalWalletValidationDTO {
        try {
            val url = "$externalServiceUrl/api/v1/wallet/validate"
            val requestBody = ExternalCvuValidationRequest(cvu)
            val response = restTemplate.postForEntity(
                url, 
                requestBody, 
                ExternalWalletValidationDTO::class.java
            )
            
            return response.body ?: throw ExternalServiceException("Empty response from external service")
        } catch (ex: HttpClientErrorException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> throw ExternalWalletNotFoundException("External CVU $cvu not found")
                else -> throw ExternalServiceException("External service error: ${ex.message}")
            }
        } catch (ex: Exception) {
            throw ExternalServiceException("Failed to connect to external wallet service: ${ex.message}")
        }
    }

    fun validateExternalBalance(cvu: Long, amount: Float): ExternalWalletValidationDTO {
        // First validate that the CVU exists
        validateExternalCvu(cvu)
        
        // Then validate the balance using the dedicated balance endpoint
        try {
            val url = "$externalServiceUrl/api/v1/wallet/validate-balance"
            val requestBody = ExternalBalanceValidationRequest(cvu, amount)
            val response = restTemplate.postForEntity(
                url, 
                requestBody, 
                ExternalWalletValidationDTO::class.java
            )
            
            val validation = response.body ?: throw ExternalServiceException("Empty response from external service")
            
            if (!validation.hasSufficientFunds) {
                throw ExternalInsufficientFundsException("External CVU $cvu has insufficient funds for amount $amount")
            }
            
            return validation
        } catch (ex: HttpClientErrorException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> throw ExternalWalletNotFoundException("External CVU $cvu not found")
                HttpStatus.BAD_REQUEST -> throw ExternalInsufficientFundsException("External CVU $cvu has insufficient funds")
                else -> throw ExternalServiceException("External service error: ${ex.message}")
            }
        } catch (ex: ExternalInsufficientFundsException) {
            throw ex // Re-throw our custom exception
        } catch (ex: Exception) {
            throw ExternalServiceException("Failed to connect to external wallet service: ${ex.message}")
        }
    }
} 