package plataya.app.model.dtos

data class UserDtoResponse(val name: String,val lastname: String,val mail: String, var token: String? = null   )