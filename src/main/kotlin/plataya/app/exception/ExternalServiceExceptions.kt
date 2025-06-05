package plataya.app.exception
 
class ExternalServiceException(message: String) : RuntimeException(message)
class ExternalWalletNotFoundException(message: String) : RuntimeException(message)
class ExternalInsufficientFundsException(message: String) : RuntimeException(message) 