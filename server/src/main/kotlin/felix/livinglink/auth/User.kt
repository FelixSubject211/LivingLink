package felix.livinglink.auth

data class User(
    val id: String,
    val username: String,
    val hashedPassword: String
)