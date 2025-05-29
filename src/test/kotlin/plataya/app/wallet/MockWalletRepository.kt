package plataya.app.wallet

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import plataya.app.model.entities.Wallet
import plataya.app.repository.WalletRepository
import java.util.Optional
import java.util.function.Function

class MockWalletRepository: WalletRepository {
    private val wallets = mutableListOf<Wallet>()

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> saveAndFlush(entity: S & Any): S & Any {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> saveAllAndFlush(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch(entities: Iterable<Wallet?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllByIdInBatch(ids: Iterable<Long?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllInBatch() {
        TODO("Not yet implemented")
    }

    override fun getOne(id: Long): Wallet {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): Wallet {
        TODO("Not yet implemented")
    }

    override fun getReferenceById(id: Long): Wallet {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> findAll(example: Example<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> findAll(
        example: Example<S?>,
        sort: Sort
    ): List<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> saveAll(entities: Iterable<S?>): List<S?> {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<Wallet?> {
        return wallets
    }

    override fun findAllById(ids: Iterable<Long?>): List<Wallet?> {
        TODO("Not yet implemented")
    }

    override fun findAll(sort: Sort): List<Wallet?> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Wallet?> {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> findOne(example: Example<S?>): Optional<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> findAll(
        example: Example<S?>,
        pageable: Pageable
    ): Page<S?> {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> count(example: Example<S?>): Long {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> exists(example: Example<S?>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?, R : Any?> findBy(
        example: Example<S?>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S?>?, R?>
    ): R & Any {
        TODO("Not yet implemented")
    }

    override fun <S : Wallet?> save(entity: S & Any): S & Any {
        if (entity != null) {
            wallets.add(entity as Wallet)
        }
        return entity
    }

    override fun findById(id: Long): Optional<Wallet?> {
        TODO("Not yet implemented")
    }

    override fun existsById(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun delete(entity: Wallet) {
        TODO("Not yet implemented")
    }

    override fun deleteAllById(ids: Iterable<Long?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: Iterable<Wallet?>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun existsByCvu(cvu: Long): Boolean {
        return wallets.any { it.cvu == cvu }
    }
}

