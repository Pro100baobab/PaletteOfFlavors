package DataSource.API

// TODO: change file location
data class EmailRequest(val email: String, val code: String)
data class ApiResponse(val success: Boolean, val error: String?)

//data class SmsRequest(val phone: String, val code: String)

