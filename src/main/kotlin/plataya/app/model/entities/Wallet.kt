package plataya.app.model.entities

import jakarta.persistence.*
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
