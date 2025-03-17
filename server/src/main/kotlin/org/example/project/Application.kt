package org.example.project

import User
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
import kotlinx.serialization.json.Json
import org.example.project.database.DatabaseFactory
import org.example.project.model.LockerRepository
import org.example.project.model.Roles
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.routes.lockerParcelRoutes
import org.example.project.routes.lockerRoutes
import org.example.project.routes.mailRoutes
import org.example.project.routes.userRoutes
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun main() {
    embeddedServer(Netty, port = 8080, host = "192.168.68.197", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    DatabaseFactory.init()
    val userRepository = UserRepository()
    val lockerRepository = LockerRepository(userRepository)

    routing {
        get("/") {
            call.respondText("Hello, Ktor with PostgreSQL!")
        }

        route("/auth") {
            userRoutes(userRepository)
        }
        route("/locker") {
            lockerRoutes(lockerRepository)
            mailRoutes(lockerRepository)
            lockerParcelRoutes(lockerRepository)
            get {
                try{
                    val locker = lockerRepository.getAllLockers()

                    call.respond(locker)
                } catch (e: Exception){
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "failed to retrieve lockers: ${e.message}")
                    )
                }

            }
        }



    route("/users") {
            get {
                try {
                    val users = userRepository.getAllUsers()
                    call.respond(users)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to retrieve users: ${e.message}")
                    )
                }
            }

            get("/{email}") {
                val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                userRepository.getUserByEmail(email)?.let {
                    call.respond(it)
                } ?: call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }

            get("/role/{role}") {
                try {
                    val role = call.parameters["role"] ?: return@get call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Role parameter is required"))

                    val roleEnum = try {
                        Roles.valueOf(role)
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(HttpStatusCode.BadRequest, 
                            mapOf("error" to "Invalid role. Valid roles are: ${Roles.entries.joinToString()}"))
                    }

                    val users = transaction {
                        User.selectAll()
                            .where { User.role eq roleEnum }
                            .map { userRepository.resultRowToUser(it) }
                    }
                    
                    call.respond(users)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to retrieve users by role: ${e.message}")
                    )
                }
            }

            post {
                try {
                    val user = call.receive<Users>()

                    // Hash the password before storing
                    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt(12))

                    // Check if email already exists
                    if (userRepository.getUserByEmail(user.email) != null) {
                        return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email is already registered"))
                    }

                    // Check if username already exists
                    if (userRepository.getUserByEmail(user.username) != null) {
                        return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to "Username is already taken"))
                    }

                    // Add user with separate username and email
                    if (userRepository.addUser(Users(user.username, user.email, hashedPassword, user.roles))) {
                        call.respond(HttpStatusCode.Created, mapOf("message" to "User created successfully"))
                    } else {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "Failed to create user"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create user: ${e.message}"))
                }
            }


            delete("/{email}") {
                val email = call.parameters["email"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (userRepository.deleteUser(email)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }
        }
    }
}
