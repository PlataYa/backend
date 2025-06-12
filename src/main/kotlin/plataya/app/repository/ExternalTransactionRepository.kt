package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import plataya.app.model.entities.transaction.ExternalTransaction

@Repository
interface ExternalTransactionRepository : JpaRepository<ExternalTransaction, Long> {
    @Query("SELECT t FROM ExternalTransaction t WHERE t.internalWallet.cvu = :cvu OR t.externalCvu = :cvu ORDER BY t.createdAt DESC")
    fun findAllByCvu(@Param("cvu") cvu: Long): List<ExternalTransaction>
} 