package plataya.app.transaction

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import plataya.app.model.entities.Transaction
import plataya.app.repository.TransactionRepository
import java.util.Optional
import java.util.function.Function

class MockTransactionRepository: TransactionRepository {
    private val transactions = mutableMapOf<Long, Transaction>()
    private var nextId = 1L

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> saveAndFlush(entity: S & Any): S & Any {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> saveAllAndFlush(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch(entities: Iterable<Transaction?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllByIdInBatch(ids: Iterable<Long?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch() {
        TODO("Not yet implemented")
    }

    override fun getOne(id: Long): Transaction {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Transaction {
        TODO("Not yet implemented")
    }

    override fun getReferenceById(id: Long): Transaction {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> findAll(example: Example<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> findAll(
        example: Example<S?>,
        sort: Sort
    ): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> saveAll(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<Transaction?> {
        return transactions.values.toList()
    }

    override fun findAllById(ids: Iterable<Long?>): List<Transaction?> {
        TODO("Not yet implemented")
    }

    override fun findAll(sort: Sort): List<Transaction?> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Transaction?> {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> findOne(example: Example<S?>): Optional<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> findAll(
        example: Example<S?>,
        pageable: Pageable
    ): Page<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> count(example: Example<S?>): Long {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> exists(example: Example<S?>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?, R : Any?> findBy(
        example: Example<S?>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S?>?, R?>
    ): R & Any {
        TODO("Not yet implemented")
    }

    override fun <S : Transaction?> save(entity: S & Any): S & Any {
        val transactionToSave = entity as Transaction
        val finalTransaction =
            if (transactionToSave.transactionId == null) {
                transactionToSave.copy(transactionId = nextId++)
            } else {
                transactionToSave
            }
        transactions[finalTransaction.transactionId!!] = finalTransaction
        return (finalTransaction as S)!!
    }

    override fun findById(id: Long): Optional<Transaction> {
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

    override fun delete(entity: Transaction) {
        entity.transactionId?.let { transactions.remove(it) }
    }

    override fun deleteAllById(ids: Iterable<Long?>) {
        ids.filterNotNull().forEach { transactions.remove(it) }
    }

    override fun deleteAll(entities: Iterable<Transaction?>) {
        entities.filterNotNull().forEach { it.transactionId?.let { id -> transactions.remove(id) } }
    }

    override fun deleteAll() {
        transactions.clear()
    }

    override fun findAllByCvu(cvu: Long): List<Transaction> {
        return transactions.values.filter { transaction ->
            transaction.payerWallet?.cvu == cvu || transaction.payeeWallet?.cvu == cvu
        }.sortedByDescending { it.createdAt }
    }
}