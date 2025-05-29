package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import plataya.app.entity.Wallet

interface WalletRepository : JpaRepository<Wallet, Long> {
    fun existsByCvu(cvu: Long): Boolean
}
