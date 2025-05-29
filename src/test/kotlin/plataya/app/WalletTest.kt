package plataya.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import plataya.app.dto.WalletDTO
import plataya.app.factory.WalletFactory

class WalletTest {
    @Test
    @DisplayName("Wallet should be created correctly")
    fun test_1() {
        val walletMock = WalletDTO("mail@mail.com", 123456789, 1863.4F)

        assertEquals("mail@mail.com", walletMock.mail)
        assertEquals(123456789, walletMock.cvu)
        assertEquals(1863.4F, walletMock.balance)
    }

    @Test
    @DisplayName("Wallet service should create a wallet with default values correctly")
    fun test_2() {
        val factory = WalletFactory()

        val createdWallet = factory.createWallet("mail@mail.com")

        assertEquals(createdWallet.cvu, 100000000001)
        assertEquals(createdWallet.balance, 0F)
    }

    @Test
    @DisplayName("Wallet service should create various wallets correctly")
    fun test_3() {
        val factory = WalletFactory()

        factory.createWallet("mail@mail.com")
        factory.createWallet("mail@mail.com")
        val createdWallet = factory.createWallet("mail@mail.com")

        assertEquals(createdWallet.cvu, 100000000003)
        assertEquals(createdWallet.balance, 0F)
    }
}
