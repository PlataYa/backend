package plataya.app.exception

class WalletNotFoundException(message: String) : RuntimeException(message)
class InsufficientFundsException(message: String) : RuntimeException(message)
class InvalidTransactionException(message: String) : RuntimeException(message)
class TransactionNotFoundException(message: String) : RuntimeException(message)

class InvalidCredentialsException(message: String) : RuntimeException(message)
