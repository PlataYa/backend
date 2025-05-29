package plataya.app.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val transactionId: Long? = null,

    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @ManyToOne
    @JoinColumn(name = "payer_cvu", referencedColumnName = "cvu")
    val payerWallet: Wallet? = null, // Nullable for deposits

    @ManyToOne
    @JoinColumn(name = "payee_cvu", referencedColumnName = "cvu")
    val payeeWallet: Wallet? = null, // Nullable for withdrawals

    val amount: Float,

    @Enumerated(EnumType.STRING)
    val currency: Currency = Currency.ARS,

    @Enumerated(EnumType.STRING)
    var status: TransactionStatus,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val externalReference: String? = null // For transactions with external systems
) 