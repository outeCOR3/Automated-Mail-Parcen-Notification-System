package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.project.model.LockerItem
import org.example.project.model.LockerRepository
import org.example.project.model.Mail
import org.example.project.model.Mails

import org.example.project.model.Notification
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId


fun Route.mailRoutes(lockerRepository: LockerRepository) {
    post("/lockers/mail/{lockerId}") {
        val lockerId = call.parameters["lockerId"]?.toIntOrNull()
        if (lockerId == null) {
            call.respond(HttpStatusCode.BadRequest, "Valid locker ID is required")
            return@post
        }

        try {
            val result = transaction {
                val locker = lockerRepository.getLockersById(lockerId).firstOrNull()
                if (locker == null) {
                    return@transaction Pair(false, "Locker with ID $lockerId not found")
                }

                val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()

                // Insert Mail
                val mailInsert = Mail.insert {
                    it[deliveredAt] = phTime
                }
                val mailId = mailInsert[Mail.id]

                // Insert LockerItem
                LockerItem.insert {
                    it[LockerItem.lockerId] = lockerId
                    it[itemId] = mailId
                    it[itemType] = "MAIL"
                }

                // Insert Notification
                Notification.insert {
                    it[userId] = locker.userId // Get userId from the locker
                    it[message] = "New mail added to your locker (ID: $lockerId) at $phTime"
                    it[createdAt] = phTime
                }

                val mail = Mails(id = mailId)
                Pair(true, mail)
            }

            if (result.first) {
                call.respond(HttpStatusCode.Created, result.second)
            } else {
                call.respond(HttpStatusCode.NotFound, result.second as String)
            }
        } catch (e: Exception) {
            println("Error adding mail to locker: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.InternalServerError, "Failed to add mail to locker: ${e.message}")
        }
    }
}
