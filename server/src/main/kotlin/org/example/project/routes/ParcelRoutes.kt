package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.project.model.LockerItem
import org.example.project.model.LockerRepository
import org.example.project.model.Notification
import org.example.project.model.Parcel
import org.example.project.model.Parcels
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun formatInstantForResponse(instant: Instant): String {
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Manila"))
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a") // Example: "Mar 17, 2025 - 08:30 PM"
    return localDateTime.format(formatter)
}

fun Route.lockerParcelRoutes(lockerRepository: LockerRepository) {
    post("/lockers/parcel/{lockerId}") {
        val lockerId = call.parameters["lockerId"]?.toIntOrNull()
        if (lockerId == null) {
            call.respond(HttpStatusCode.BadRequest, "Valid locker ID is required")
            return@post
        }

        val parcelData = try {
            call.receive<Parcels>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid request body: tracking number is required")
            return@post
        }

        if (parcelData.trackingNumber.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Tracking number cannot be empty")
            return@post
        }

        try {
            val result = transaction {
                val locker = lockerRepository.getLockersById(lockerId).firstOrNull()
                if (locker == null) {
                    return@transaction Pair(false, "Locker with ID $lockerId not found")
                }

                val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()
                val formattedTime = formatInstantForResponse(phTime) // Convert to readable format

                // Insert Parcel
                val parcelInsert = Parcel.insert {
                    it[trackingNumber] = parcelData.trackingNumber
                    it[deliveredAt] = phTime
                    it[imageUrl] = parcelData.imageUrl
                }
                val parcelId = parcelInsert[Parcel.id]

                // Insert LockerItem
                LockerItem.insert {
                    it[LockerItem.lockerId] = lockerId
                    it[itemId] = parcelId
                    it[itemType] = "PARCEL"
                }

                // ✅ Insert Notification with formatted date
                Notification.insert {
                    it[userId] = locker.userId // Get userId from the locker
                    it[message] = "📦 New parcel (Tracking: ${parcelData.trackingNumber}) added to your locker (ID: $lockerId) at $formattedTime"
                    it[createdAt] = phTime // Still store `Instant` in DB
                }

                val parcel = Parcels(id = parcelId, trackingNumber = parcelData.trackingNumber, imageUrl = parcelData.imageUrl)
                Pair(true, parcel)
            }

            if (result.first) {
                call.respond(HttpStatusCode.Created, result.second)
            } else {
                call.respond(HttpStatusCode.NotFound, result.second as String)
            }
        } catch (e: Exception) {
            println("Error adding parcel to locker: ${e.stackTraceToString()}")
            call.respond(HttpStatusCode.InternalServerError, "Failed to add parcel to locker: ${e.message}")
        }
    }
}