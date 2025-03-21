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
import org.example.project.model.LockerRepository
import org.example.project.model.Lockers
import org.example.project.model.UserRepository

fun Route.lockerRoutes(lockerRepository: LockerRepository,userRepository: UserRepository) {

    // Get all lockers
    get("/lockers") {
        val lockers = lockerRepository.getAllLockers()

        println("✅ Retrieved lockers: $lockers") // Debugging output

        if (lockers.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, lockers)
        } else {
            call.respond(HttpStatusCode.NoContent, "No lockers found")
        }
    }
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

        // Find the user based on the email
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
                call.respond(HttpStatusCode.Created, "Locker added successfully for user ID: ${lockerData.userId}")
            } else {
                call.respond(HttpStatusCode.Conflict, "Could not add locker: User not found or locker already exists.")
            }
        } catch (e: Exception) {
            println("Error parsing request: ${e.message}")
            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
        }
    }

    // Delete a locker by ID
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