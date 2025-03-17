package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.example.project.model.LockerRepository
import org.example.project.model.LockingAction


fun Route.lockerLockingRoutes(lockerRepository: LockerRepository) {

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

    get("/lockers/{id}") {
        val lockerId = call.parameters["id"]?.toIntOrNull()
        if (lockerId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid locker ID")
            return@get
        }

        val locker = lockerRepository.getLockersByLockerId(lockerId).firstOrNull()
        if (locker == null) {
            call.respond(HttpStatusCode.NotFound, "Locker with ID $lockerId not found")
        } else {
            call.respond(HttpStatusCode.OK, locker)
        }
    }
}
