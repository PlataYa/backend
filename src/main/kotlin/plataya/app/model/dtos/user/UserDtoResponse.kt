package plataya.app.model.dtos.user

data class UserDtoResponse(
    val name: String,
    val lastname: String,
    val mail: String,
    var token: String? = null,
    var cvu: Long? = null
)