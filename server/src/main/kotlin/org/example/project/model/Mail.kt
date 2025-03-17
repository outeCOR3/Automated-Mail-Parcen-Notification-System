package org.example.project.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Mail : Table() {
    val id = integer("id").autoIncrement()
    val deliveredAt = timestamp("delivered_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}
