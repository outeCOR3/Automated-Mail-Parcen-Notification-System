package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.example.project.model.LockerRepository
import org.example.project.model.Lockers

fun Route.lockerRoutes(lockerRepository: LockerRepository) {

    // Get all lockers
    get("/locker") {
        val lockers = lockerRepository.getAllLockers()

        println("✅ Retrieved lockers: $lockers") // Debugging output

        if (lockers.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, lockers)
        } else {
            call.respond(HttpStatusCode.NoContent, "No lockers found")
        }
    }

    // Get lockers by user ID
    get("/lockers/user/{userId}") {
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
        val lockerData = call.receive<Lockers>()
        val userId = lockerData.userId

        // Check if user exists and locker can be added
        val isAdded = lockerRepository.addLocker(userId)

        if (isAdded) {
            call.respond(HttpStatusCode.Created, "Locker added successfully for user ID: $userId")
        } else {
            call.respond(
                HttpStatusCode.Conflict,
                "Could not add locker: User not found or locker already exists."
            )
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
    put("/lockers/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        val updateData = call.receive<Map<String, String>>()
        val newUserId = updateData["user_id"]?.toIntOrNull()

        if (id == null || newUserId == null) {
            call.respond(HttpStatusCode.BadRequest, "Valid locker ID and new user ID are required")
            return@put
        }

        val isUpdated = lockerRepository.updateLocker(id, newUserId)

        if (isUpdated) {
            call.respond(HttpStatusCode.OK, "Locker updated successfully for ID: $id")
        } else {
            call.respond(
                HttpStatusCode.Conflict,
                "Failed to update locker. User might already have a locker or data is incorrect."
            )
        }
    }
}
