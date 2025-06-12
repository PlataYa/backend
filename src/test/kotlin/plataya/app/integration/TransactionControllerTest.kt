package plataya.app.integration

import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import plataya.app.TestEnvironmentInitializer
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestEnvironmentInitializer::class])
@ActiveProfiles("test")
class TransactionControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private fun createUserAndGetCvu(userEmail: String, userName: String = "Test", userLastName: String = "User", dob: String = "01-01-2000"): Long {
        val userJson = """{
            "name": "$userName",
            "lastname": "$userLastName",
            "mail": "$userEmail",
            "password": "Password123!",
            "dayOfBirth": "$dob"
        }"""
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userJson))
            .andExpect(MockMvcResultMatchers.status().isOk)

        val mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/wallet/mine?mail=$userEmail"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
        
        val responseBody = mvcResult.response.contentAsString
        return JsonPath.parse(responseBody).read<Number>("$.cvu").toLong()
    }

    private fun defaultDeposit(destinationCvu: Long, amount: Double, externalRef: String = "ref123"): Long {
        val depositJson = """{"sourceCvu":200000000001, "destinationCvu":$destinationCvu,"amount":$amount,"currency":"ARS","externalReference":"$externalRef"}"""
        val mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(depositJson))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()
        val responseBody = mvcResult.response.contentAsString
        return JsonPath.parse(responseBody).read<Number>("$.transactionId").toLong()
    }

    @Test
    @Transactional
    fun `get transaction by id returns 200`() {
        val user1Cvu = createUserAndGetCvu("user.gettxbyid@example.com", "Getter", "TX")
        val transactionId = defaultDeposit(user1Cvu, 150.0, "depositForGetById")

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transaction/transfer/$transactionId"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.transactionId").value(transactionId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.type").value("DEPOSIT"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payeeCvu").value(user1Cvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(150.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    @Transactional
    fun `get transaction history by cvu returns 200`() {
        val userCvu = createUserAndGetCvu("user.txhistory@example.com", "History", "User")
        defaultDeposit(userCvu, 50.0, "depositForHistory1")
        defaultDeposit(userCvu, 75.0, "depositForHistory2")

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transaction/$userCvu/history"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].amount").value(75.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].amount").value(50.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].payeeCvu").value(userCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].payeeCvu").value(userCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("DEPOSIT"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("DEPOSIT"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("COMPLETED"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("COMPLETED"))

    }

    @Test
    @Transactional
    fun `create deposit returns 201`() {
        val destinationCvu = createUserAndGetCvu("user.deposit@example.com", "Depositor", "Account")
        val sourceCvu = 200000000001
        val amount = 200.0
        val externalReference = "Banco Nación"
        val depositJson = """{"sourceCvu":$sourceCvu,"destinationCvu":$destinationCvu,"amount":$amount,"currency":"ARS","externalReference":"$externalReference"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(depositJson))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.type").value("DEPOSIT"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceCvu").value(sourceCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationCvu").value(destinationCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(amount))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.externalReference").value(externalReference))
    }

    @Test
    @Transactional
    fun `create withdrawal returns 201`() {
        val sourceCvu = createUserAndGetCvu("user.withdraw@example.com", "Withdrawer", "Client")
        val destinationCvu = "200000000001"
        defaultDeposit(sourceCvu, 100.0, "initialDepositForWithdrawal")

        val withdrawalAmount = 30.0
        val externalReference = "Banco Nación"
        val withdrawalJson = """{"sourceCvu":$sourceCvu, "destinationCvu":$destinationCvu,"amount":$withdrawalAmount,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/withdrawal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(withdrawalJson))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.type").value("WITHDRAWAL"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceCvu").value(sourceCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationCvu").value(destinationCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(withdrawalAmount))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.externalReference").value(externalReference))
    }

    @Test
    @Transactional
    fun `create p2p transfer returns 201`() {
        val payerCvu = createUserAndGetCvu("user.payer@example.com", "Payer", "P2P")
        val payeeCvu = createUserAndGetCvu("user.payee@example.com", "Payee", "P2P")

        defaultDeposit(payerCvu, 200.0, "initialDepositForP2P")

        val transferAmount = 70.0
        val transferJson = """{"payerCvu":$payerCvu,"payeeCvu":$payeeCvu,"amount":$transferAmount,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.type").value("P2P"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payerCvu").value(payerCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.payeeCvu").value(payeeCvu))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(transferAmount))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"))
    }

    // --- Exception Handling Tests ---

    @Test
    @Transactional
    fun `createP2PTransfer with non-existent payer CVU returns 404 WalletNotFound`() {
        val payeeCvu = createUserAndGetCvu("ex.payee.walletnotfound@example.com", "Payee", "WNF")
        val nonExistentCvu = 999999999999L
        val transferJson = """{"payerCvu":$nonExistentCvu,"payeeCvu":$payeeCvu,"amount":50.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("Payer wallet with ID $nonExistentCvu not found."))
    }

    @Test
    @Transactional
    fun `createP2PTransfer with non-existent payee CVU returns 404 WalletNotFound`() {
        val payerCvu = createUserAndGetCvu("ex.payer.walletnotfound@example.com", "Payer", "WNF")
        defaultDeposit(payerCvu, 100.0, "depositForWNFPayeeTest")
        val nonExistentCvu = 999999999998L
        val transferJson = """{"payerCvu":$payerCvu,"payeeCvu":$nonExistentCvu,"amount":50.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("Payee wallet with ID $nonExistentCvu not found."))
    }

    @Test
    @Transactional
    fun `createDeposit to non-existent CVU returns 404 WalletNotFound`() {
        val nonExistentCvu = 999999999997L
        val depositJson = """{"sourceCvu":200000000001,"destinationCvu":$nonExistentCvu,"amount":100.0,"currency":"ARS","externalReference":"wnfDeposit"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(depositJson))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("Internal wallet with ID $nonExistentCvu not found."))
    }

    @Test
    @Transactional
    fun `createWithdrawal from non-existent CVU returns 404 WalletNotFound`() {
        val nonExistentCvu = 999999999996L
        val withdrawalJson = """{"sourceCvu":$nonExistentCvu,"destinationCvu":200000000001,"amount":50.0,"currency":"ARS","externalReference":"wnfWithdraw"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/withdrawal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(withdrawalJson))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("Internal wallet with ID $nonExistentCvu not found."))
    }
    
    @Test
    fun `getTransactionHistory for non-existent CVU returns 404 WalletNotFound`() {
        val nonExistentCvu = 999999999995L
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transaction/$nonExistentCvu/history"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("Wallet with CVU $nonExistentCvu not found."))
    }

    @Test
    @Transactional
    fun `createP2PTransfer with insufficient funds returns 400 InsufficientFunds`() {
        val payerCvu = createUserAndGetCvu("ex.payer.insufficient@example.com", "Payer", "Funds")
        val payeeCvu = createUserAndGetCvu("ex.payee.insufficient@example.com", "Payee", "Funds")
        // No deposit for payer

        val transferJson = """{"payerCvu":$payerCvu,"payeeCvu":$payeeCvu,"amount":100.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Payer Funds's wallet has insufficient funds."))
    }

    @Test
    @Transactional
    fun `createWithdrawal with insufficient funds returns 400 InsufficientFunds`() {
        val sourceCvu = createUserAndGetCvu("ex.withdraw.insufficient@example.com", "Withdrawer", "Poor")
        defaultDeposit(sourceCvu, 20.0, "smallDeposit") // Deposit less than withdrawal amount

        val withdrawalJson = """{"sourceCvu":$sourceCvu,"destinationCvu":200000000001,"amount":50.0,"currency":"ARS","externalReference":"insufficientWithdraw"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/withdrawal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(withdrawalJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Withdrawer Poor's wallet has insufficient funds."))
    }

    @Test
    @Transactional
    fun `createP2PTransfer with same payer and payee CVU returns 400 InvalidTransaction`() {
        val userCvu = createUserAndGetCvu("ex.user.samecvu@example.com", "User", "SameCVU")
        defaultDeposit(userCvu, 100.0, "depositForSameCVU")

        val transferJson = """{"payerCvu":$userCvu,"payeeCvu":$userCvu,"amount":50.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Payer and payee CVU cannot be the same."))
    }

    @Test
    @Transactional
    fun `createP2PTransfer with negative amount returns 400 InvalidTransaction`() {
        val payerCvu = createUserAndGetCvu("ex.payer.negative@example.com", "Payer", "Neg")
        val payeeCvu = createUserAndGetCvu("ex.payee.negative@example.com", "Payee", "Neg")
        defaultDeposit(payerCvu, 100.0, "depositForNegativeP2P")

        val transferJson = """{"payerCvu":$payerCvu,"payeeCvu":$payeeCvu,"amount":-50.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("P2P Transfer amount must be positive."))
    }
    
    @Test
    @Transactional
    fun `createP2PTransfer with zero amount returns 400 InvalidTransaction`() {
        val payerCvu = createUserAndGetCvu("ex.payer.zero@example.com", "Payer", "Zero")
        val payeeCvu = createUserAndGetCvu("ex.payee.zero@example.com", "Payee", "Zero")
        defaultDeposit(payerCvu, 100.0, "depositForZeroP2P")

        val transferJson = """{"payerCvu":$payerCvu,"payeeCvu":$payeeCvu,"amount":0.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(transferJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("P2P Transfer amount must be positive."))
    }


    @Test
    @Transactional
    fun `createDeposit with negative amount returns 400 InvalidTransaction`() {
        val destinationCvu = createUserAndGetCvu("ex.deposit.negative@example.com", "Depositor", "Neg")
        val depositJson = """{"sourceCvu":200000000001,"destinationCvu":$destinationCvu,"amount":-100.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(depositJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Deposit amount must be positive."))
    }
    
    @Test
    @Transactional
    fun `createDeposit with zero amount returns 400 InvalidTransaction`() {
        val payeeCvu = createUserAndGetCvu("ex.deposit.zero@example.com", "Depositor", "Zero")
        val depositJson = """{"payeeCvu":$payeeCvu,"amount":0.0,"currency":"ARS","externalReference":"zeroDeposit"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(depositJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Deposit amount must be positive."))
    }

    @Test
    @Transactional
    fun `createWithdrawal with negative amount returns 400 InvalidTransaction`() {
        val sourceCvu = createUserAndGetCvu("ex.withdraw.negative@example.com", "Withdrawer", "Neg")
        defaultDeposit(sourceCvu, 100.0, "depositForNegWithdraw")

        val withdrawalJson = """{"sourceCvu":$sourceCvu,"destinationCvu":200000000001,"amount":-50.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/withdrawal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(withdrawalJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Withdrawal amount must be positive."))
    }
    
    @Test
    @Transactional
    fun `createWithdrawal with zero amount returns 400 InvalidTransaction`() {
        val sourceCvu = createUserAndGetCvu("ex.withdraw.zero@example.com", "Withdrawer", "Zero")
        defaultDeposit(sourceCvu, 100.0, "depositForZeroWithdraw")

        val withdrawalJson = """{"sourceCvu":$sourceCvu,"destinationCvu":200000000001,"amount":0.0,"currency":"ARS"}"""

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction/withdrawal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(withdrawalJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Withdrawal amount must be positive."))
    }

    @Test
    fun `getTransactionById for non-existent ID returns 404 TransactionNotFound`() {
        val nonExistentTxId = 999999L
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transaction/transfer/$nonExistentTxId"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().string("Transaction with ID $nonExistentTxId not found."))
    }
}
