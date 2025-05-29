package plataya.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import plataya.app.model.dto.WalletDTO
import plataya.app.service.WalletService

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
    @DisplayName("Wallet service should create a wallet correctly")
    fun test_2() {
        val service = WalletService()

        // val createdWallet = service.createWallet(WalletDTO("mail@mail.com", 123456789, 1863.4F))
    }
}
