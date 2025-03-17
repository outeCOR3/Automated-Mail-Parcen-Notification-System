package org.example.project.model

import User
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Locker : Table() {
    val id = integer("id").autoIncrement()  // Auto-increment primary key
    val locker_id = integer("locker_id").uniqueIndex().autoIncrement() // Unique locker ID
    val user_id = reference("user_id", User.id)  // Foreign key reference to User
    val createdAt = timestamp("created_at")
    val isLocked = bool("is_locked").default(false)  // Default: Unlocked

    override val primaryKey = PrimaryKey(id)
}
