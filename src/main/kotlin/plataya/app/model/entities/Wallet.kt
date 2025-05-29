package plataya.app.model.entities

import jakarta.persistence.*

@Entity
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    val cvu: Long,

    @OneToOne
    val user: User,

    val balance: Float
)
