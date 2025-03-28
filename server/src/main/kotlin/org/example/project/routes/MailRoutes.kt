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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


// Function to format `Instant` into a readable string
fun formatInstantForResponse(instant: Instant): String {
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Manila"))
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a") // Example: "Mar 17, 2025 - 08:30 PM"
    return localDateTime.format(formatter)
}

fun Route.mailRoutes(lockerRepository: LockerRepository) {
    post("/lockers/mail/{lockerId}") {
        val lockerId = call.parameters["lockerId"]?.toIntOrNull()
        if (lockerId == null) {
            call.respond(HttpStatusCode.BadRequest, "Valid locker ID is required")
            return@post
        }

        try {
            val result = transaction {
                val locker = lockerRepository.getLockersByLockerId(lockerId).firstOrNull()
                if (locker == null) {
                    return@transaction Pair(false, "Locker with ID $lockerId not found")
                }

                val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()
                val formattedTime = formatInstantForResponse(phTime) // Convert to readable format

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

                // ✅ Insert Notification with formatted date
                Notification.insert {
                    it[userId] = locker.userId // Get userId from the locker
                    it[message] = "📩 New mail added to your locker (ID: $lockerId) at $formattedTime"
                    it[createdAt] = phTime // Still store Instant in DB
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
