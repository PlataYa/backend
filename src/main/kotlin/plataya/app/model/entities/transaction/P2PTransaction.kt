package plataya.app.model.entities.transaction

import jakarta.persistence.*
import plataya.app.model.entities.wallet.Wallet
import java.time.LocalDateTime

@Entity
@Table(name = "p2p_transactions")
data class P2PTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val transactionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_cvu", referencedColumnName = "cvu", nullable = false)
    val payerWallet: Wallet,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_cvu", referencedColumnName = "cvu", nullable = false)
    val payeeWallet: Wallet,

    override val amount: Float,

    @Enumerated(EnumType.STRING)
    override val currency: Currency = Currency.ARS,

    @Enumerated(EnumType.STRING)
    override val status: TransactionStatus,

    override val createdAt: LocalDateTime = LocalDateTime.now()
) : TransactionEntity {
    
    override fun getTransactionType(): TransactionType = TransactionType.P2P
} 