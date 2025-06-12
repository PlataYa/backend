package plataya.app.transaction

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import plataya.app.model.entities.transaction.P2PTransaction
import plataya.app.repository.P2PTransactionRepository
import java.util.Optional
import java.util.function.Function

class MockP2PTransactionRepository : P2PTransactionRepository {
    private val transactions = mutableMapOf<Long, P2PTransaction>()
    private var nextId = 1L

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> saveAndFlush(entity: S & Any): S & Any {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> saveAllAndFlush(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch(entities: Iterable<P2PTransaction?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllByIdInBatch(ids: Iterable<Long?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch() {
        TODO("Not yet implemented")
    }

    override fun getOne(id: Long): P2PTransaction {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): P2PTransaction {
        TODO("Not yet implemented")
    }

    override fun getReferenceById(id: Long): P2PTransaction {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> findAll(example: Example<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> findAll(
        example: Example<S?>,
        sort: Sort
    ): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> saveAll(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<P2PTransaction?> {
        return transactions.values.toList()
    }

    override fun findAllById(ids: Iterable<Long?>): List<P2PTransaction?> {
        TODO("Not yet implemented")
    }

    override fun findAll(sort: Sort): List<P2PTransaction?> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<P2PTransaction?> {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> findOne(example: Example<S?>): Optional<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> findAll(
        example: Example<S?>,
        pageable: Pageable
    ): Page<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> count(example: Example<S?>): Long {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> exists(example: Example<S?>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?, R : Any?> findBy(
        example: Example<S?>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S?>?, R?>
    ): R & Any {
        TODO("Not yet implemented")
    }

    override fun <S : P2PTransaction?> save(entity: S & Any): S & Any {
        val transaction = entity as P2PTransaction
        val savedTransaction = if (transaction.transactionId == null) {
            transaction.copy(transactionId = nextId++)
        } else {
            transaction
        }
        transactions[savedTransaction.transactionId!!] = savedTransaction
        return (savedTransaction as S)!!
    }

    override fun findById(id: Long): Optional<P2PTransaction> {
        return Optional.ofNullable(transactions[id])
    }

    override fun existsById(id: Long): Boolean {
        return transactions.containsKey(id)
    }

    override fun count(): Long {
        return transactions.size.toLong()
    }

    override fun deleteById(id: Long) {
        transactions.remove(id)
    }

    override fun delete(entity: P2PTransaction) {
        entity.transactionId?.let { transactions.remove(it) }
    }

    override fun deleteAllById(ids: Iterable<Long?>) {
        ids.filterNotNull().forEach { transactions.remove(it) }
    }

    override fun deleteAll(entities: Iterable<P2PTransaction?>) {
        entities.filterNotNull().forEach { it.transactionId?.let { id -> transactions.remove(id) } }
    }

    override fun deleteAll() {
        transactions.clear()
    }

    override fun findAllByCvu(cvu: Long): List<P2PTransaction> {
        return transactions.values.filter { transaction ->
            transaction.payerWallet.cvu == cvu || transaction.payeeWallet.cvu == cvu
        }.sortedByDescending { it.createdAt }
    }

    // Helper method for testing
    fun clearAll() {
        transactions.clear()
        nextId = 1L
    }
} 