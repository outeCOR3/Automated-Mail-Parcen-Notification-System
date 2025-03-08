package org.example.project



import org.example.project.model.UserRepository
import org.mindrot.jbcrypt.BCrypt

actual suspend fun LoginManager(private val userRepository: UserRepository) {

    suspend fun login(email: String, password: String): String {
        val userEmail = email.lowercase().trim()

        // Fetch user from database
        val user = userRepository.getUserByEmail(userEmail)

        if (user == null) {
            println("DEBUG: User not found for email: $userEmail")
            return "Login failed: Invalid email or password"
        }

        println("DEBUG: Found user: ${user.email}, Stored Hashed Password: ${user.password}")

        // Verify password
        return if (BCrypt.checkpw(password, user.password)) {
            "Login successful!"
        } else {
            println("DEBUG: Password mismatch")
            "Login failed: Invalid email or password"
        }
    }
}