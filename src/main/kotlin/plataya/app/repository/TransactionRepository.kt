package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import plataya.app.model.entities.Transaction

interface TransactionRepository : JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.payerWallet.cvu = :cvu OR t.payeeWallet.cvu = :cvu ORDER BY t.createdAt DESC")
    fun findAllByCvu(@Param("cvu") cvu: Long): List<Transaction>
} 