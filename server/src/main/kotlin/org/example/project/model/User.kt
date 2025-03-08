package org.example.project.model
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp


object User : Table("users") {
    val id = integer("id").autoIncrement() // Auto-increment primary key
    val username = text("username")
    val email = text("email")
    val passwordHash = text("password_hash")
    val role = enumerationByName("role",10,Roles::class)
    val createdAt = timestamp("created_at")
    // Define created_at column as timestamp

    override val primaryKey = PrimaryKey(id)
}
