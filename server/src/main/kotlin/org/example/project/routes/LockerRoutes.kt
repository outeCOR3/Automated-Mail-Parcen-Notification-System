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
    get("/lockers") {
        val lockers = lockerRepository.getAllLockers()
        call.respond(lockers)
    }

    // Get locker by sensor ID
    get("/lockers/{sensorId}") {
        val sensorId = call.parameters["sensorId"]
        if (sensorId == null) {
            call.respond(HttpStatusCode.BadRequest, "Sensor ID is required")
            return@get
        }

        val locker = lockerRepository.getLockerBySensorId(sensorId)
        if (locker != null) {
            call.respond(locker)
        } else {
            call.respond(HttpStatusCode.NotFound, "Locker not found")
        }
    }

    // Get lockers by username
    get("/lockers/user/{username}") {
        val username = call.parameters["username"]
        if (username == null) {
            call.respond(HttpStatusCode.BadRequest, "Username is required")
            return@get
        }

        val lockers = lockerRepository.getLockersByUsername(username)
        call.respond(lockers)
    }

    // Add a new locker
    post("/lockers") {
        val lockerData = call.receive<Lockers>()

        val isAdded = lockerRepository.addLocker(lockerData.sensorId, lockerData.username)
        if (isAdded) {
            call.respond(HttpStatusCode.Created, "Locker added successfully")
        } else {
            call.respond(HttpStatusCode.Conflict, "Locker could not be added")
        }
    }

    // Delete a locker by sensor ID
    delete("/lockers/{sensorId}") {
        val sensorId = call.parameters["sensorId"]
        if (sensorId == null) {
            call.respond(HttpStatusCode.BadRequest, "Sensor ID is required")
            return@delete
        }

        val isDeleted = lockerRepository.deleteLocker(sensorId)
        if (isDeleted) {
            call.respond(HttpStatusCode.OK, "Locker deleted successfully")
        } else {
            call.respond(HttpStatusCode.NotFound, "Locker not found")
        }
    }

    // Update a locker (change username)
    put("/lockers/{sensorId}") {
        val sensorId = call.parameters["sensorId"]
        val updateData = call.receive<Map<String, String>>()
        val newUsername = updateData["username"]

        if (sensorId == null || newUsername.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Sensor ID and new username are required")
            return@put
        }

        val isUpdated = lockerRepository.updateLocker(sensorId, newUsername)
        if (isUpdated) {
            call.respond(HttpStatusCode.OK, "Locker updated successfully")
        } else {
            call.respond(HttpStatusCode.NotFound, "Locker not found")
        }
    }

}
