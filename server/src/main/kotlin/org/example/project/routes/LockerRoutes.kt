package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable


import org.example.project.model.LockerRepository
import org.example.project.model.Lockers
import org.example.project.model.Notification

import org.example.project.model.NotificationRepository

import org.example.project.model.UserRepository

import org.jetbrains.exposed.sql.transactions.transaction



fun Route.lockerRoutes(lockerRepository: LockerRepository,userRepository: UserRepository,notificationRepository: NotificationRepository) {

    // Get all lockers

    get("/me") {
        val principal = call.principal<JWTPrincipal>()
        println("principal: $principal")

        // Retrieve the email from the JWT token (sub claim contains email)
        val email = principal?.payload?.getClaim("sub")?.asString()
        println("User email from token: $email")  // Log for debugging

        if (email == null) {
            call.respond(HttpStatusCode.Unauthorized, "User email missing or invalid token")
            return@get
        }

        // Find the user ba
        // sed on the email
        val user = userRepository.getUserByEmail(email)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
            return@get
        }

        val userId = user.id

        // Get lockers for the user by userId
        val lockers = lockerRepository.getLockersById(userId)
        if (lockers.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, lockers)
        } else {
            call.respond(HttpStatusCode.NotFound, "No lockers found for user ID: $userId")
        }
    }


    get("/notifications/{userId}") {
        // Retrieve userId from the route parameters
        val userId = call.parameters["userId"]?.toIntOrNull()

        // Log userId for debugging
        println("User ID from route: $userId")

        if (userId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            return@get
        }

        try {
            // Get user based on the userId
            val user = userRepository.getUserById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@get
            }

            // Get notifications for the user by userId
            val notifications = notificationRepository.getNotificationsByUserId(userId)

            // Log notifications for debugging
            println("Notifications for user ID $userId: $notifications")

            if (notifications.isEmpty()) {
                call.respond(HttpStatusCode.NoContent, "No notifications found for user.")
            } else {
                call.respond(HttpStatusCode.OK, notifications)
            }
        } catch (e: Exception) {
            e.printStackTrace()  // Print stack trace for detailed error debugging
            println("Error occurred: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
        }
    }










    // Get lockers by user ID
    get("/users/{userId}/lockers") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.BadRequest, "User ID is required")
            return@get
        }

        val lockers = lockerRepository.getLockersById(userId)
        if (lockers.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, lockers)
        } else {
            call.respond(HttpStatusCode.NotFound, "No lockers found for user ID: $userId")
        }

    }

    // Add a new locker
    post("/lockers") {
        try {
            val lockerData = call.receive<Lockers>()
            val isAdded = lockerRepository.addLocker(lockerData.userId, lockerData.isLocked)

            if (isAdded) {
                call.respond(
                    HttpStatusCode.Created,
                    "Locker added successfully for user ID: ${lockerData.userId}"
                )
            } else {
                call.respond(
                    HttpStatusCode.Conflict,
                    "Could not add locker: User not found or locker already exists."
                )
            }
        } catch (e: Exception) {
            println("Error parsing request: ${e.message}")
            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
        }
    }
}


    // Delete a locker by ID
