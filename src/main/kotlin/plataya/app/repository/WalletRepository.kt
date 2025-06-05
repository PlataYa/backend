package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plataya.app.model.entities.Wallet
import plataya.app.model.entities.User

@Repository
interface WalletRepository : JpaRepository<Wallet, Long> {
    fun existsByCvu(cvu: Long): Boolean
    fun findByUser(user: User): Wallet?
}
