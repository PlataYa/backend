package plataya.app.wallet

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import plataya.app.exception.InsufficientFundsException
import plataya.app.exception.WalletNotFoundException
import plataya.app.factory.WalletFactory
import plataya.app.model.dtos.wallet.WalletDTO
import plataya.app.model.entities.user.User
import plataya.app.service.WalletService

class WalletTest {
    val mockUser = User(
        mail = "mail@mail.com",
        name = "Test",
        lastname = "User",
        password = "password",
        dayOfBirth = "2000-01-01",
    )

    @Test
    @DisplayName("Wallet should be created correctly")
    fun test_1() {
        val walletMock = WalletDTO("mail@mail.com", 123456789, 1863.4F)

        Assertions.assertEquals("mail@mail.com", walletMock.userMail)
        Assertions.assertEquals(123456789, walletMock.cvu)
        Assertions.assertEquals(1863.4F, walletMock.balance)
    }

    @Test
    @DisplayName("Wallet factory should create a wallet with default values correctly")
    fun test_2() {
        val factory = WalletFactory()

        val createdWallet = factory.createWalletEntity(mockUser)

        Assertions.assertEquals(createdWallet.cvu, 100000000001)
        Assertions.assertEquals(createdWallet.balance, 0F)
    }

    @Test
    @DisplayName("Wallet factory should create various wallets correctly")
    fun test_3() {
        val factory = WalletFactory()

        factory.createWalletEntity(mockUser)
        factory.createWalletEntity(mockUser)
        val createdWallet = factory.createWalletEntity(mockUser)

        Assertions.assertEquals(createdWallet.cvu, 100000000003)
        Assertions.assertEquals(createdWallet.balance, 0F)
    }

    @Test
    @DisplayName("Wallet service should save wallet correctly")
    fun test_4() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        service.createWallet(mockUser)
        val allWallets = service.getAllWallets()

        Assertions.assertEquals(1, allWallets.wallets.size)
        Assertions.assertEquals("mail@mail.com", allWallets.wallets[0].userMail)
    }

    @Test
    @DisplayName("Wallet service should update balance correctly")
    fun test_5() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        service.createWallet(mockUser)
        val updatedWallet = service.updateBalance(100000000001, 100F)

        Assertions.assertEquals(100F, updatedWallet.balance)
        Assertions.assertEquals(100000000001, updatedWallet.cvu)
    }

    @Test
    @DisplayName("Wallet service should throw exception when searching for non-existent wallet")
    fun test_6() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        Assertions.assertThrows(WalletNotFoundException::class.java) {
            service.getWalletByCvu(-1526)
        }
    }

    @Test
    @DisplayName("Wallet service validate CVU should return true for existing CVU")
    fun test_7() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        service.createWallet(mockUser)
        val validationResponse = service.validateCvu(100000000001)

        Assertions.assertTrue(validationResponse.valid)
    }

    @Test
    @DisplayName("Wallet service validate CVU should return false for non-existing CVU")
    fun test_8() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        val validationResponse = service.validateCvu(-1526)

        Assertions.assertFalse(validationResponse.valid)
    }

    @Test
    @DisplayName("Wallet service should return existing wallet by CVU correctly")
    fun test_9() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        service.createWallet(mockUser)
        val wallet = service.getWalletByCvu(100000000001)

        Assertions.assertEquals("mail@mail.com", wallet.userMail)
    }

    @Test
    @DisplayName("Wallet service should return balance by CVU correctly")
    fun test_10() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        service.createWallet(mockUser)
        val balance = service.getBalanceByCvu(100000000001)

        Assertions.assertEquals(100000000001, balance.cvu)
        Assertions.assertEquals(0F, balance.balance)
    }

    @Test
    @DisplayName("Wallet service should throw exception when updating balance of non-existent wallet")
    fun test_11() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        Assertions.assertThrows(WalletNotFoundException::class.java) {
            service.updateBalance(-1526, 100F)
        }
    }

    @Test
    @DisplayName("Wallet service should throw exception when trying to update balance with insufficient funds")
    fun test_12() {
        val factory = WalletFactory()
        val mockRepo = MockWalletRepository()

        val service = WalletService(factory, mockRepo)

        service.createWallet(mockUser)

        Assertions.assertThrows(InsufficientFundsException::class.java) {
            service.updateBalance(100000000001, -100F)
        }
    }
}
