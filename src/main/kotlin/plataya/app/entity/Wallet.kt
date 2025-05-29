package plataya.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "wallet")
data class Wallet(
    @Id
    @Column(unique = true)
    val cvu: Long,
    val mail: String,
    val balance: Float
)
