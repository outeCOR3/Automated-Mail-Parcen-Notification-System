package org.example.project.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Notification : Table("notifications") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id",Locker.user_id)
    val message = text("message")
    val createdAt = timestamp("createdAt")

    override val primaryKey = PrimaryKey(id)
}
