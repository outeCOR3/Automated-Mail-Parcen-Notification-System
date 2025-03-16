package org.example.project.database

import User
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.project.model.Locker
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(hikari())
        transaction {
            SchemaUtils.create(User, Locker) // ✅ Create "users" table if it doesn't exist
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/ampns"
            driverClassName = "org.postgresql.Driver"
            username = "postgres"
            password = "jennierubyjanekim"
            maximumPoolSize = 10
            isAutoCommit = true

            validate()
        }
        return HikariDataSource(config)
    }
}
