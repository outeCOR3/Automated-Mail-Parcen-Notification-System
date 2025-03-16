package org.example.project.model

import User
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Locker : Table() {
    val id = integer("id").autoIncrement()
    val sensorId = varchar("sensor_id", 100).uniqueIndex()
    val username = reference("username", User.username) // Foreign key referencing User table
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
