package org.example.project.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object LockerItem : Table() {
    val id = integer("id").autoIncrement()
    val lockerId = reference("locker_id", Locker.id)
    val itemId = integer("item_id")
    val itemType = varchar("item_type", 50) // Assuming ENUM is stored as String
    
    override val primaryKey = PrimaryKey(id)
}
