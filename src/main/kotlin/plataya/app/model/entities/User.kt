package plataya.app.model.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val lastname: String,

    @Column( nullable = false, unique = true)
    val mail: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val dayOfBirth: String,
)
