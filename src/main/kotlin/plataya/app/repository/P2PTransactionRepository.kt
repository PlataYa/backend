package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import plataya.app.model.entities.transaction.P2PTransaction

@Repository
interface P2PTransactionRepository : JpaRepository<P2PTransaction, Long> {
    @Query("SELECT t FROM P2PTransaction t WHERE t.payerWallet.cvu = :cvu OR t.payeeWallet.cvu = :cvu ORDER BY t.createdAt DESC")
    fun findAllByCvu(@Param("cvu") cvu: Long): List<P2PTransaction>
} 