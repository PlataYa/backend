package plataya.app.model.entities.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

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