package plataya.app.repository

import org.springframework.data.jpa.repository.JpaRepository
import plataya.app.entity.Transaction

interface TransactionRepository : JpaRepository<Transaction, Long> 