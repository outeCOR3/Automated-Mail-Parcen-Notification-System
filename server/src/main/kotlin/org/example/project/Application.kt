package org.example.project

import User
import com.auth0.jwt.JWT
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.example.project.database.DatabaseFactory
import org.example.project.model.LockerRepository
import org.example.project.model.Lockers
import org.example.project.model.LockingAction
import org.example.project.model.NotificationRepository
import org.example.project.model.Roles
import org.example.project.model.UserRepository
import org.example.project.model.Users
import org.example.project.routes.lockerLockingRoutes
import org.example.project.routes.lockerParcelRoutes
import org.example.project.routes.lockerRoutes
import org.example.project.routes.mailRoutes
import org.example.project.routes.userRoutes
import org.example.project.security.JwtConfig
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun main() {

    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)

}
    fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(Authentication) {
        jwt("jwt-auth") {
            verifier(
                JWT.require(JwtConfig.algorithm)
                    .withIssuer(JwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("role").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is invalid or expired")
            }
        }
    }

    DatabaseFactory.init()
    val userRepository = UserRepository()
    val lockerRepository = LockerRepository(userRepository)
    val notificationRepository = NotificationRepository(userRepository)

    routing {
        get("/") {
            call.respondText("Hello, Ktor with PostgreSQL!")
        }

        route("/auth") {
            userRoutes(userRepository)
        }

        authenticate("jwt-auth") {
            route("/locker") {
                requireRole("User") {
                    lockerRoutes(lockerRepository,userRepository,notificationRepository)
                    lockerLockingRoutes(lockerRepository)
                    mailRoutes(lockerRepository)
                    lockerParcelRoutes(lockerRepository)
                    get {
                        try {
                            val locker = lockerRepository.getAllLockers()
                            call.respond(locker)
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "failed to retrieve lockers: ${e.message}")
                            )
                        }
                    }
                }
            }
        }

        authenticate("jwt-auth") {
            lockerRoutes(lockerRepository,userRepository,notificationRepository)
            route("/users") {
                    get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    val email = principal?.payload?.subject
                    if (email == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                        return@get
                    }

                    val user = userRepository.getUserByEmail(email)
                    if (user != null) {
                        call.respond(user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                  }

                requireRole("Admin") {
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

                    route("/lockerAll") {
                        get {
                            val lockers = lockerRepository.getAllLockers()
                            if (lockers.isNotEmpty()) {
                                call.respond(HttpStatusCode.OK, lockers)
                            } else {
                                call.respond(HttpStatusCode.NoContent, "No lockers found")
                            }
                        }

                        get("/status") {
                            val statuses = lockerRepository.getAllLockersStatus()
                            call.respond(HttpStatusCode.OK, statuses)
                        }
                    }

                    post("/lockers/lock") {
                        val lockingAction = call.receive<LockingAction>()

                        // Check if locker exists using locker_id
                        val lockerExists = lockerRepository.getLockersByLockerId(lockingAction.id).isNotEmpty()
                        if (!lockerExists) {
                            call.respond(HttpStatusCode.NotFound, "Locker with ID ${lockingAction.id} not found")
                            return@post
                        }

                        // Process lock/unlock action
                        val isUpdated = lockerRepository.updateLockerLockState(
                            lockerId = lockingAction.id,  // locker_id reference
                            isLocked = lockingAction.isLocked
                        )

                        if (isUpdated) {
                            call.respond(HttpStatusCode.OK, "Locker ${lockingAction.id} has been ${if (lockingAction.isLocked) "locked" else "unlocked"}.")
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Failed to update locker status")
                        }
                    }
                    post("/lockers") {
                        try {
                            val lockerData = call.receive<Lockers>()
                            val isAdded = lockerRepository.addLocker(lockerData.userId, lockerData.isLocked)

                            if (isAdded) {
                                call.respond(HttpStatusCode.Created, "Locker added successfully for user ID: ${lockerData.userId}")
                            } else {
                                call.respond(HttpStatusCode.Conflict, "Could not add locker: User not found or locker already exists.")
                            }
                        } catch (e: Exception) {
                            println("Error parsing request: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                        }
                    }


                    get("/{email}") {
                        val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                        userRepository.getUserByEmail(email)?.let {
                            call.respond(it)
                        } ?: call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }

                    delete("/lockers/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Valid locker ID is required")
                            return@delete
                        }

                        val isDeleted = lockerRepository.deleteLocker(id)
                        if (isDeleted) {
                            call.respond(HttpStatusCode.OK, "Locker deleted successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Locker not found")
                        }
                    }

                    // Update a locker (change user ID)
                    put("/lockers/{lockerId}") {
                        val lockerId = call.parameters["lockerId"]?.toIntOrNull()
                        val updateData = call.receive<Map<String, String>>()
                        val newUserId = updateData["user_id"]?.toIntOrNull()

                        if (lockerId == null || newUserId == null) {
                            call.respond(HttpStatusCode.BadRequest, "Valid locker ID and new user ID are required")
                            return@put
                        }

                        val isUpdated = lockerRepository.updateLocker(lockerId, newUserId)

                        if (isUpdated) {
                            call.respond(HttpStatusCode.OK, "Locker updated successfully for locker_id: $lockerId")
                        } else {
                            call.respond(
                                HttpStatusCode.Conflict,
                                "Failed to update locker. User might already have a locker or data is incorrect."
                            )
                        }
                    }
                }

                    get("/role/{role}") {
                        try {
                            val role = call.parameters["role"] ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Role parameter is required")
                            )

                            val roleEnum = try {
                                Roles.valueOf(role)
                            } catch (e: IllegalArgumentException) {
                                return@get call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Invalid role. Valid roles are: ${Roles.entries.joinToString()}")
                                )
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
                            if (userRepository.addUser(Users(user.id,user.username, user.email, hashedPassword, user.roles))) {
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
    }


fun Route.requireRole(role: String, build: Route.() -> Unit): Route {
    return createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
    }).apply {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val userRole = principal?.payload?.getClaim("role")?.asString()
            if (userRole != role) {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to access this resource")
                finish()
            }
        }
        build()
    }
}

