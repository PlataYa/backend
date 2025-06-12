package plataya.app.client

import plataya.app.model.dtos.externalwallet.ExternalWalletValidationDTO
import plataya.app.model.dtos.externalwallet.ExternalCvuValidationDTO
import plataya.app.model.dtos.transaction.ExternalDepositResponse
import plataya.app.model.entities.transaction.Currency

interface ExternalWalletClientInterface {
    fun validateExternalCvu(cvu: Long): ExternalCvuValidationDTO
    fun validateExternalBalance(cvu: Long, amount: Float): ExternalWalletValidationDTO
    fun makeExternalDeposit(destinationCvu: Long, amount: Float, currency: Currency, reference: String): ExternalDepositResponse
} 