package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plataya.app.model.entities.wallet.Wallet
import plataya.app.model.entities.user.User

@Repository
interface WalletRepository : JpaRepository<Wallet, Long> {
    fun existsByCvu(cvu: Long): Boolean

    fun findWalletByUserMail(mail: String): Wallet?
  
    fun findByUser(user: User): Wallet?

}
