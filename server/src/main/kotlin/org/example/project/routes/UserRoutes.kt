package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.example.project.model.Roles
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.security.JwtConfig
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

fun Route.userRoutes(userRepository: UserRepository) {
    // Register route
    post("/register") {
        try {
            val userData = call.receive<RegisterRequest>()

            // Validate input
            if (!isValidEmail(userData.email)) {
                return@post call.respond(HttpStatusCode.BadRequest, "Invalid email format")
            }
            if (userData.password.length < 8) {
                return@post call.respond(HttpStatusCode.BadRequest, "Password must be at least 8 characters")
            }

            // Check if user already exists
            if (userRepository.getUserByEmail(userData.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "User already exists")
            }

            // Hash the password before saving
            val hashedPassword = BCrypt.hashpw(userData.password, BCrypt.gensalt(12)) // Increased salt rounds

            // Save user to repository
            userRepository.addUser(Users(userData.email, hashedPassword, Roles.User))
            call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
        } catch (e: Exception) {
            println("Registration error: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.InternalServerError, "Server error during registration")
        }
    }

    // Login route
    post("/login") {
        try {
            val credentials = call.receive<LoginRequest>()
            println("Received login attempt for email: ${credentials.email}")

            val user = userRepository.getUserByEmail(credentials.email)
            println("User found: ${user != null}")

            if (user == null) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }

            // Verify password
            if (!BCrypt.checkpw(credentials.password, user.password)) {
                println("Password verification failed for user: ${user.email}")
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }

            // Generate JWT token
            val token = JwtConfig.generateToken(user.email)
            call.respond(mapOf("token" to token, "role" to user.roles.name))
        } catch (e: Exception) {
            println("Login error: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"
    return email.matches(Regex(emailRegex))
}