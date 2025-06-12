package plataya.app.model.entities.wallet

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import plataya.app.model.entities.user.User

@Entity
data class Wallet(
    @Id
    @Column(unique = true)
    val cvu: Long,

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User,

    val balance: Float
)