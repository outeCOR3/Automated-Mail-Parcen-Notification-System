package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.project.model.LoginRequest
import org.example.project.model.RegisterUserRequest
import org.example.project.model.Roles
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.security.JwtConfig
import org.mindrot.jbcrypt.BCrypt

fun Route.userRoutes(userRepository: UserRepository) {
    post("/register") {
        try {
            val userData = call.receive<RegisterUserRequest>()

            // Validate email format
            if (!isValidEmail(userData.email)) {
                return@post call.respond(HttpStatusCode.BadRequest, "Invalid email format")
            }

            // Ensure password length is at least 8 characters
            if (userData.password.length < 8) {
                return@post call.respond(HttpStatusCode.BadRequest, "Password must be at least 8 characters")
            }

            // Ensure username is not empty
            if (userData.username.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, "Username cannot be empty")
            }

            // Check if email already exists
            if (userRepository.getUserByEmail(userData.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "User with this email already exists")
            }

            // Check if username already exists
            if (userRepository.getUserByUsername(userData.username) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Username is already taken")
            }

            // Hash password securely
            val hashedPassword = BCrypt.hashpw(userData.password, BCrypt.gensalt(12))

            // Save user with a separate username and email
            val user = Users(userData.username, userData.email, hashedPassword, Roles.User)
            val isAdded = userRepository.addUser(user)

            if (isAdded) {
                call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "User could not be created")
            }
        } catch (e: Exception) {
            println("Registration error: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.InternalServerError, "Server error during registration")
        }
    }

    post("/login") {
        try {
            val credentials = call.receive<LoginRequest>()
            val normalizedEmail = credentials.email.lowercase() // Normalize email

            println("[DEBUG] Login attempt for: $normalizedEmail")

            // Retrieve user by email
            val user = userRepository.getUserByEmail(normalizedEmail)
            println("[DEBUG] Retrieved user: ${user?.email}")

            if (user == null) {
                println("[ERROR] User not found")
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }

            // Debug logs for password verification
            println("[DEBUG] Stored hash: ${user.password}")
            println("[DEBUG] Input password: ${credentials.password}")

            // Validate password against hashed password
            val passwordMatches = BCrypt.checkpw(credentials.password, user.password)
            println("[DEBUG] Password match: $passwordMatches")

            if (!passwordMatches) {
                println("[ERROR] Password mismatch")
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }

            // Generate JWT token
            val token = JwtConfig.generateToken(user.email)
            call.respond(mapOf("token" to token, "role" to user.roles.name))
        } catch (e: Exception) {
            println("Login error: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.BadRequest, "Login failed: ${e.message}")
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"
    return email.matches(Regex(emailRegex))
}
