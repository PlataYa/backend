package plataya.app.model.entities.transaction

import jakarta.persistence.*
import plataya.app.model.entities.wallet.Wallet
import java.time.LocalDateTime

@Entity
@Table(name = "external_transactions")
data class ExternalTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val transactionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_cvu", referencedColumnName = "cvu", nullable = false)
    val internalWallet: Wallet, // The wallet in our system involved in the transaction

    @Column(nullable = false)
    val externalReference: String,

    @Column(nullable = false)
    val externalCvu: Long, // External CVU (source for deposits, destination for withdrawals)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private val transactionType: TransactionType, // DEPOSIT or WITHDRAWAL

    override val amount: Float,

    @Enumerated(EnumType.STRING)
    override val currency: Currency = Currency.ARS,

    @Enumerated(EnumType.STRING)
    override val status: TransactionStatus,

    override val createdAt: LocalDateTime = LocalDateTime.now()
) : TransactionEntity {
    override fun getTransactionType(): TransactionType = transactionType
}