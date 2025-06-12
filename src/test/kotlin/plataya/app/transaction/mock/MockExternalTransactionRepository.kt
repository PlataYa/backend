package plataya.app.transaction

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import plataya.app.model.entities.transaction.ExternalTransaction
import plataya.app.repository.ExternalTransactionRepository
import java.util.Optional
import java.util.function.Function

class MockExternalTransactionRepository : ExternalTransactionRepository {
    private val transactions = mutableMapOf<Long, ExternalTransaction>()
    private var nextId = 1000L

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> saveAndFlush(entity: S & Any): S & Any {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> saveAllAndFlush(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch(entities: Iterable<ExternalTransaction?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllByIdInBatch(ids: Iterable<Long?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch() {
        TODO("Not yet implemented")
    }

    override fun getOne(id: Long): ExternalTransaction {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): ExternalTransaction {
        TODO("Not yet implemented")
    }

    override fun getReferenceById(id: Long): ExternalTransaction {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> findAll(example: Example<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> findAll(
        example: Example<S?>,
        sort: Sort
    ): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> saveAll(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<ExternalTransaction?> {
        return transactions.values.toList()
    }

    override fun findAllById(ids: Iterable<Long?>): List<ExternalTransaction?> {
        TODO("Not yet implemented")
    }

    override fun findAll(sort: Sort): List<ExternalTransaction?> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<ExternalTransaction?> {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> findOne(example: Example<S?>): Optional<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> findAll(
        example: Example<S?>,
        pageable: Pageable
    ): Page<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> count(example: Example<S?>): Long {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> exists(example: Example<S?>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?, R : Any?> findBy(
        example: Example<S?>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S?>?, R?>
    ): R & Any {
        TODO("Not yet implemented")
    }

    override fun <S : ExternalTransaction?> save(entity: S & Any): S & Any {
        val transaction = entity as ExternalTransaction
        val savedTransaction = if (transaction.transactionId == null) {
            transaction.copy(transactionId = nextId++)
        } else {
            transaction
        }
        transactions[savedTransaction.transactionId!!] = savedTransaction
        return (savedTransaction as S)!!
    }

    override fun findById(id: Long): Optional<ExternalTransaction> {
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

    override fun delete(entity: ExternalTransaction) {
        entity.transactionId?.let { transactions.remove(it) }
    }

    override fun deleteAllById(ids: Iterable<Long?>) {
        ids.filterNotNull().forEach { transactions.remove(it) }
    }

    override fun deleteAll(entities: Iterable<ExternalTransaction?>) {
        entities.filterNotNull().forEach { it.transactionId?.let { id -> transactions.remove(id) } }
    }

    override fun deleteAll() {
        transactions.clear()
    }

    override fun findAllByCvu(cvu: Long): List<ExternalTransaction> {
        return transactions.values.filter { transaction ->
            transaction.internalWallet.cvu == cvu
        }.sortedByDescending { it.createdAt }
    }

    // Helper method for testing
    fun clearAll() {
        transactions.clear()
        nextId = 1000L
    }
} 