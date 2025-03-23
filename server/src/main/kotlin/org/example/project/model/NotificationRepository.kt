package org.example.project.model

import NotificationDTO
import kotlinx.datetime.Instant
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


class NotificationRepository(private val userRepository: UserRepository) {
    // Convert ResultRow to NotificationDTO
    val currentTime =
        LocalDateTime.now().atZone(ZoneId.of("Asia/Manila")).toInstant() //
    private fun resultRowToNotification(row: ResultRow): NotificationDTO {
        val message = row[Notification.message]
        val createdAtInstant = row[Notification.createdAt] // Get timestamp from DB
        val userId = row[Notification.userId]

        // Convert `java.time.Instant` (DB) to `kotlinx.datetime.Instant`
        val kotlinxCreatedAt = Instant.fromEpochMilliseconds(createdAtInstant.toEpochMilli())

        return NotificationDTO(
            message = message,
            createdAt = kotlinxCreatedAt,  // Use correctly converted Instant
            userId = userId
        )
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

