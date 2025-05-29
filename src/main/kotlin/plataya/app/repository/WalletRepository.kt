package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plataya.app.entity.Wallet

@Repository
interface WalletRepository : JpaRepository<Wallet, Long> {
}
