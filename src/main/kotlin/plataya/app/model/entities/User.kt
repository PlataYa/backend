package plataya.app.model.entities

import jakarta.persistence.*

@Entity
@Table(name = "plataya_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,
    val surname: String,

    @Column(unique = true)
    val mail: String,

    val password: String
)