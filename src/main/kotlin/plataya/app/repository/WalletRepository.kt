package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import plataya.app.model.entities.Wallet

interface WalletRepository : JpaRepository<Wallet, Long> {
    fun existsByCvu(cvu: Long): Boolean
}
