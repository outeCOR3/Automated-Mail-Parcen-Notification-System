package org.example.project.model

import User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LockerRepository(private val userRepository: UserRepository) {

    private fun resultRowToLocker(row: ResultRow): Lockers = Lockers(
        id = row[Locker.id],
        sensorId = row[Locker.sensorId],
        username = row[Locker.username], // Fetching username from User table

    )

    fun getAllLockers(): List<Lockers> = transaction {
        println("Fetching all lockers with usernames from User table...")

        (Locker innerJoin User)
            .select(Locker.id, Locker.sensorId, User.username) // ✅ Explicitly select username
            .map { row ->
                Lockers(
                    id = row[Locker.id],
                    sensorId = row[Locker.sensorId],
                    username = row[User.username] // ✅ Correct reference
                )
            }
    }

    fun getLockerBySensorId(sensorId: String): Lockers? = transaction {
        (Locker innerJoin User)
            .select(Locker.id, Locker.sensorId, User.username) // ✅ Include username
            .where { Locker.sensorId eq sensorId }
            .map { row ->
                Lockers(
                    id = row[Locker.id],
                    sensorId = row[Locker.sensorId],
                    username = row[User.username] // ✅ Fetch username correctly
                )
            }
            .singleOrNull()
    }

    fun getLockersByUsername(username: String): List<Lockers> = transaction {
        (Locker innerJoin User)
            .select(Locker.id, Locker.sensorId, User.username) // ✅ Include username
            .where { User.username eq username }
            .map { row ->
                Lockers(
                    id = row[Locker.id],
                    sensorId = row[Locker.sensorId],
                    username = row[User.username] // ✅ Fetch username correctly
                )
            }
    }


    fun addLocker(sensorId: String, username: String): Boolean = transaction {
        println("Checking if user with username $username exists...")

        val user = userRepository.getUserByUsername(username) ?: return@transaction false

        val currentTime = java.time.Instant.now()

        Locker.insert {
            it[Locker.sensorId] = sensorId
            it[Locker.username] = username  // ✅ Use username directly
            it[createdAt] = currentTime
        }

        println("Added locker with sensor ID: $sensorId for user: $username at $currentTime (UTC)")
        true
    }



    fun deleteLocker(sensorId: String): Boolean = transaction {
        Locker.deleteWhere { Locker.sensorId eq sensorId } > 0
    }

    fun updateLocker(sensorId: String, newEmail: String): Boolean = transaction {
        val user = userRepository.getUserByEmail(newEmail)

        if (user != null) {
            val newUsername = user.username

            val updatedRows = Locker.update({ Locker.sensorId eq sensorId }) {
                it[username] = newUsername // Update username based on User table
            }

            println("Updated locker with sensor ID: $sensorId to new username: $newUsername (from email: $newEmail)")
            return@transaction updatedRows > 0
        } else {
            println("User with email $newEmail does not exist. Locker update failed.")
            return@transaction false
        }
    }
}
