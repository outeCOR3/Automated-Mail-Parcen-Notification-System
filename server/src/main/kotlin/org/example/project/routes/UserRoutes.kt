package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.project.model.Roles
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.security.JwtConfig
import org.mindrot.jbcrypt.BCrypt

// User registration and login routes
fun Route.userRoutes(userRepository: UserRepository) {
    // Register route
    post("/register") {
        val userData = call.receive<Map<String, String>>()
        val email = userData["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val password = userData["password"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        // Check if user already exists
        if (userRepository.getUserByEmail(email) != null) {
            return@post call.respond(HttpStatusCode.Conflict)
        }

        // Hash the password before saving
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        // Save user to repository
        userRepository.addUser(Users(email, hashedPassword, Roles.User))
        call.respond(HttpStatusCode.Created)
    }

    // Login route
    post("/login") {
        val credentials = call.receive<Map<String, String>>()
        val email = credentials["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val password = credentials["password"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        // Retrieve user by email
        val user = userRepository.getUserByEmail(email)

        // Verify password
        if (user != null && BCrypt.checkpw(password, user.password)) {
            val token = JwtConfig.generateToken(email)
            call.respond(mapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}
