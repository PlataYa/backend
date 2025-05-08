package plataya.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import plataya.app.dto.WalletDTO

class WalletTest {
    @Test
    fun correctCreationOfWallet() {
        val walletMock = WalletDTO("mail@mail.com", 123456789, 1863.4F)

        assertEquals("mail@mail.com", walletMock.mail)
        assertEquals(123456789, walletMock.cvu)
        assertEquals(1863.4F, walletMock.balance)
    }
}