package org.example.project.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Parcel : Table() {
    val id = integer("id").autoIncrement()
    val trackingNumber = varchar("tracking_number", 255).uniqueIndex()
    val deliveredAt = timestamp("delivered_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}
