package plataya.app.model.dtos

data class UserDtoRequest(val name: String, val lastname: String, val mail: String, val password: String, val dayOfBirth: String)

data class LoginRequest(val mail: String, val password: String)

data class RegisterRequest(val name: String, val lastname: String, val mail: String, val password: String, val dayOfBirth: String)
