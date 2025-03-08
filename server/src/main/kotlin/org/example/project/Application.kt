package org.example.project

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.example.project.database.DatabaseFactory
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.routes.userRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "192.168.68.173", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    DatabaseFactory.init()
    val userRepository = UserRepository()

    routing {
        get("/") {
            call.respondText("Hello, Ktor with PostgreSQL!")
        }

        // Authentication routes
        route("/auth") {
            userRoutes(userRepository)  // Handles /auth/register and /auth/login
        }

        // User management routes
        route("/users") {
            get {
                try {
                    call.respond(HttpStatusCode.OK, userRepository.getAllUsers())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve users")
                }
            }

            get("/{email}") {
                val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                userRepository.getUserByEmail(email)?.let {
                    call.respond(it)
                } ?: call.respond(HttpStatusCode.NotFound, "User not found")
            }

            post {
                try {
                    val user = call.receive<Users>()
                    if (userRepository.addUser(user)) {
                        call.respond(HttpStatusCode.Created)
                    } else {
                        call.respond(HttpStatusCode.Conflict)
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            delete("/{email}") {
                val email = call.parameters["email"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (userRepository.deleteUser(email)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}