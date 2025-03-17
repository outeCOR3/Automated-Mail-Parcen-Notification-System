package org.example.project.model

import User
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Locker : Table() {
    val id = integer("id").autoIncrement()
    val locker_id = integer("locker_id").uniqueIndex().autoIncrement()
    val user_id = reference("user_id", User.id)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
