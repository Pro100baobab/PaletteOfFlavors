package DataSource.model

data class RegistrationRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String)



//data class UsersDetails(val fullName: String, val username: String, val email: String, val phoneNumber: String, val password: String)
