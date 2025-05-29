package plataya.app.model.entities

import jakarta.persistence.*
import plataya.app.model.entities.User

@Entity
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    val cvu: Long,

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User,

    val balance: Float
)
