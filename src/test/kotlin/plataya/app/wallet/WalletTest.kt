package plataya.app.wallet

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import plataya.app.factory.WalletFactory
import plataya.app.model.dtos.WalletDTO
import plataya.app.model.entities.User
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
        val allWallets = mockRepo.findAll()

        Assertions.assertEquals(1, allWallets.size)
        Assertions.assertEquals("mail@mail.com", allWallets[0]?.user?.mail)
    }
}
