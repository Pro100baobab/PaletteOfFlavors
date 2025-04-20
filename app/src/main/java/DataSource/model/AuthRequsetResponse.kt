package DataSource.model

data class RegistrationRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String)
