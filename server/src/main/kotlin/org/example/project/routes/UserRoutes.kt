package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.project.model.LoginRequest
import org.example.project.model.RegisterRequest
import org.example.project.model.Roles
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.security.JwtConfig
import org.mindrot.jbcrypt.BCrypt

fun Route.userRoutes(userRepository: UserRepository) {
    post("/register") {
        try {
            val userData = call.receive<RegisterRequest>()

            if (!isValidEmail(userData.email)) {
                return@post call.respond(HttpStatusCode.BadRequest, "Invalid email format")
            }
            if (userData.password.length < 8) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Password must be at least 8 characters"
                )
            }

            if (userRepository.getUserByEmail(userData.email) != null) {
                return@post call.respond(HttpStatusCode.Conflict, "User already exists")
            }

            val hashedPassword = BCrypt.hashpw(userData.password, BCrypt.gensalt(12))

            userRepository.addUser(Users(userData.email, hashedPassword, Roles.User))
            call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
        } catch (e: Exception) {
            println("Registration error: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.InternalServerError, "Server error during registration")
        }
    }

    post("/login") {
        try {
            val credentials = call.receive<LoginRequest>()
            val user = userRepository.getUserByEmail(credentials.email)

            if (user == null || !BCrypt.checkpw(credentials.password, user.password)) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }

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
