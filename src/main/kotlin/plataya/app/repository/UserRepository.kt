package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plataya.app.model.entities.User
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByMail(mail: String): User?
}