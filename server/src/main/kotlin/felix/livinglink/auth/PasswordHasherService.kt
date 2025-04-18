package felix.livinglink.auth

import org.mindrot.jbcrypt.BCrypt

interface PasswordHasherService {
    fun hashPassword(password: String): String
    fun verifyPassword(password: String, hashedPassword: String): Boolean
}

class PasswordHasherDefaultService : PasswordHasherService {
    override fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    override fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}