package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plataya.app.entity.User

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByMail(mail: String): User?
}