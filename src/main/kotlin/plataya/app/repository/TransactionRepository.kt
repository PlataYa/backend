package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import plataya.app.model.entities.Transaction

interface TransactionRepository : JpaRepository<Transaction, Long> 