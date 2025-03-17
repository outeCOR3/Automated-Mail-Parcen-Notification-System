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

    // Get lockers by username
    get("/lockers/user/{username}") {
        val username = call.parameters["username"]
        if (username.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Username is required")
            return@get
        }

        val lockers = lockerRepository.getLockersByUsername(username)
        call.respond(lockers)
    }

    // Add a new locker
    post("/lockers") {
        val lockerData = call.receive<Lockers>()
        val username = lockerData.username

        // Check if username exists and locker can be added
        val isAdded = lockerRepository.addLocker(username)

        if (isAdded) {
            call.respond(HttpStatusCode.Created, "Locker added successfully for user: $username")
        } else {
            // If the user doesn't exist or locker already exists
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

    // Update a locker (change username)
    put("/lockers/{username}") {
        // Retrieve the current username from the URL parameter
        val username = call.parameters["username"]
        // Retrieve the new username from the request body
        val updateData = call.receive<Map<String, String>>()
        val newUsername = updateData["username"]

        // Ensure both the current username and new username are provided
        if (username.isNullOrBlank() || newUsername.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Valid username and new username are required")
            return@put
        }

        // Find the locker associated with the current username
        val lockers = lockerRepository.getLockersByUsername(username)

        if (lockers.isEmpty()) {
            call.respond(HttpStatusCode.NotFound, "User with username $username not found")
            return@put
        }

        // Extract the locker ID from the found locker (assuming first match)
        val lockerId = lockers.first().id

        // Ensure the locker ID is not null
        lockerId?.let { id ->
            // Proceed with updating the locker using the new username
            val isUpdated = lockerRepository.updateLocker(id, newUsername)

            if (isUpdated) {
                call.respond(HttpStatusCode.OK, "Locker username updated successfully")
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to update locker username. Please check if the locker exists or the data is correct."
                )
            }
        } ?: run {
            // If the locker ID is null
            call.respond(HttpStatusCode.BadRequest, "Locker ID is required")
        }
    }
}