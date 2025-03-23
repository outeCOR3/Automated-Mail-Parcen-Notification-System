package org.example.project.model

import NotificationDTO
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.Instant as JavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class NotificationRepository(private val userRepository: UserRepository) {
    // Convert ResultRow to NotificationDTO
    val currentTime =
        LocalDateTime.now().atZone(ZoneId.of("Asia/Manila")).toInstant() //
    private fun resultRowToNotification(row: ResultRow): NotificationDTO {
        val message = row[Notification.message]
        val createdAtInstant = row[Notification.createdAt] // Get timestamp from DB
        val userId = row[Notification.userId]

        // Convert Java Instant to kotlinx.datetime.Instant
        val kotlinxCreatedAt = Instant.fromEpochMilliseconds(createdAtInstant.toEpochMilli())

        // ✅ Convert to String without T and Z
        val cleanCreatedAt = kotlinxCreatedAt.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

        // ✅ Format human-readable timestamp
        val formattedCreatedAt = formatInstant(createdAtInstant)

        return NotificationDTO(
            message = message,
            userId = userId,
            createdAt = cleanCreatedAt,  // No more T and Z!
            createdAtFormatted = formattedCreatedAt
        )
    }


    // Convert Java Instant to LocalDateTime and format it
    private fun formatInstant(instant: JavaInstant): String {
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Manila"))
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a") // Example: "Mar 23, 2025 - 08:30 AM"
        return localDateTime.format(formatter)
    }

    // Get all notifications
    fun getAllNotifications(): List<NotificationDTO> = transaction {
        println("Fetching all notifications explicitly...")
        // Directly select specific columns from the table
        Notification.selectAll()
            .map { row ->
                println("Row fetched: ${row[Notification.message]}")  // Log message column explicitly
                resultRowToNotification(row)
            }
    }

    // Add a new notification
    fun addNotification(userId: Int, message: String): Boolean = transaction {
        println("Checking if user with id: $userId exists...")
        val user = userRepository.getUserById(userId) ?: return@transaction false

        val phTime = JavaInstant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()
        Notification.insert {
            it[Notification.userId] = userId
            it[Notification.message] = message
            it[Notification.createdAt] = phTime // java.sql.Timestamp compatible
        }

        println("Added notification for user: $userId at $phTime (UTC+8)")
        true
    }

    // Delete a notification by ID
    fun deleteNotification(id: Int): Boolean = transaction {
        val deletedRows = Notification.deleteWhere { Notification.id eq id }
        println("Deleted $deletedRows notification(s) with id: $id")
        deletedRows > 0
    }

    // Update a notification's message
    fun updateNotification(id: Int, newMessage: String): Boolean = transaction {
        val updatedRows = Notification.update({ Notification.id eq id }) {
            it[Notification.message] = newMessage
        }
        println("Updated notification with id: $id to message: $newMessage")
        updatedRows > 0
    }

    // Update notification with new user ID
    fun updateNotificationUser(id: Int, newUserId: Int): Boolean = transaction {
        println("Checking if user with id: $newUserId exists...")
        val user = userRepository.getUserById(newUserId) ?: return@transaction false

        val updatedRows = Notification.update({ Notification.id eq id }) {
            it[Notification.userId] = newUserId
        }
        println("Updated notification with id: $id to new user ID: $newUserId")
        updatedRows > 0
    }

    // Get notifications by user ID
    fun getNotificationsByUserId(userId: Int): List<NotificationDTO> = transaction {
        Notification.selectAll() .where{ Notification.userId eq userId }  // Correct filtering
            .map(::resultRowToNotification)  // Direct mapping
    }
}

