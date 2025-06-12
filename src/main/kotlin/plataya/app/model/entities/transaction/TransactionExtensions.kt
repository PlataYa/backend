package plataya.app.model.entities.transaction

import plataya.app.model.dtos.transaction.ExternalTransactionResponse
import plataya.app.model.dtos.transaction.TransactionResponse

// Extension functions to convert entities to specific response types
fun P2PTransaction.toResponse(): TransactionResponse {
    return TransactionResponse(
        transactionId = this.transactionId!!,
        type = this.getTransactionType(),
        payerCvu = this.payerWallet.cvu,
        payeeCvu = this.payeeWallet.cvu,
        amount = this.amount,
        currency = this.currency,
        status = this.status,
        createdAt = this.createdAt
    )
}

fun ExternalTransaction.toTransactionResponse(): TransactionResponse {
    return when (this.getTransactionType()) {
        TransactionType.DEPOSIT -> {
            // For deposits: money flows FROM external CVU TO internal wallet
            TransactionResponse(
                transactionId = this.transactionId!!,
                type = this.getTransactionType(),
                payerCvu = this.externalCvu,
                payeeCvu = this.internalWallet.cvu,
                amount = this.amount,
                currency = this.currency,
                status = this.status,
                createdAt = this.createdAt
            )
        }
        TransactionType.WITHDRAWAL -> {
            // For withdrawals: money flows FROM internal wallet TO external CVU
            TransactionResponse(
                transactionId = this.transactionId!!,
                type = this.getTransactionType(),
                payerCvu = this.internalWallet.cvu,
                payeeCvu = this.externalCvu,
                amount = this.amount,
                currency = this.currency,
                status = this.status,
                createdAt = this.createdAt
            )
        }
        else -> throw IllegalArgumentException("Unsupported transaction type: ${this.getTransactionType()}")
    }
}

fun ExternalTransaction.toResponse(): ExternalTransactionResponse {
    return when (this.getTransactionType()) {
        TransactionType.DEPOSIT -> {
            // For deposits: money flows FROM external CVU TO internal wallet
            ExternalTransactionResponse(
                transactionId = this.transactionId!!,
                type = this.getTransactionType(),
                sourceCvu = this.externalCvu,
                destinationCvu = this.internalWallet.cvu,
                externalReference = this.externalReference,
                amount = this.amount,
                currency = this.currency,
                status = this.status,
                createdAt = this.createdAt
            )
        }
        TransactionType.WITHDRAWAL -> {
            // For withdrawals: money flows FROM internal wallet TO external CVU
            ExternalTransactionResponse(
                transactionId = this.transactionId!!,
                type = this.getTransactionType(),
                sourceCvu = this.internalWallet.cvu,
                destinationCvu = this.externalCvu,
                externalReference = this.externalReference,
                amount = this.amount,
                currency = this.currency,
                status = this.status,
                createdAt = this.createdAt
            )
        }
        else -> throw IllegalArgumentException("Unsupported transaction type: ${this.getTransactionType()}")
    }
}

// Extension function to convert any TransactionEntity to TransactionResponse
fun TransactionEntity.toResponse(): TransactionResponse {
    return when (this) {
        is P2PTransaction -> this.toResponse()
        is ExternalTransaction -> this.toTransactionResponse()
    }
} 