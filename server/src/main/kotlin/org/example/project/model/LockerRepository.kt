package org.example.project.model

import User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.ZoneId

class LockerRepository(private val userRepository: UserRepository) {

    private fun resultRowToLocker(row: ResultRow): Lockers = Lockers(
        id = row[Locker.id],
        user_id = row[User.id]
    )

    fun getAllLockers(): List<Lockers> = transaction {
        println("Fetching all lockers with usernames from User table...")
        (Locker innerJoin User)
            .select(Locker.id, User.id)
            .map(::resultRowToLocker)
    }

    fun getLockersById(id: Int): List<Lockers> = transaction {
        (Locker innerJoin User)
            .select(Locker.id, User.id)
            .where { User.id eq id }
            .map(::resultRowToLocker)
    }

    fun addLocker(id: Int): Boolean = transaction {
        println("Checking if user with id: $id exists...")
        val user = userRepository.getUserById(id) ?: return@transaction false

        val existingLocker =
            Locker.select(Locker.user_id).where { Locker.user_id eq user_id }.singleOrNull()
        if (existingLocker != null) {
            println("Locker already exists for user: $id")
            return@transaction false
        }

        val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila"))
        Locker.insert {
            it[Locker.user_id] = user_id
            it[createdAt] = phTime.toInstant()
        }

        println("Added locker for user: $id at $phTime (UTC+8)")
        true
    }

    fun deleteLocker(id: Int): Boolean = transaction {
        Locker.deleteWhere { Locker.id eq id } > 0
    }

    // Ensure this method is returning a nullable User


    fun updateLocker(id: Int, newUserId: Int): Boolean = transaction {
        // Fetch the user based on the new email provided
        val user = userRepository.getUserById(newUserId)

        // Check if the user exists
        if (user != null) {
            // Extract the username from the found user
            val newUserId = user.id

            // Check if the username is already assigned to another locker
            val existingLocker = Locker.select(Locker.user_id)
                .where ( Locker.user_id eq newUserId and (Locker.id neq id))
                .singleOrNull()

            if (existingLocker != null) {
                println("Username $newUserId is already assigned to another locker.")
                return@transaction false
            }

            // Proceed with updating the locker with the new username
            val updatedRows = Locker.update({ Locker.id eq id }) {
                it[id] = newUserId  // Set the new username from the user
            }

            // Log the successful update
            println("Updated locker with ID: $id to new username: $newUserId (from email: $newUserId)")

            // Return true if the update was successful (i.e., at least one row was updated)
            return@transaction updatedRows > 0
        } else {
            // If the user with the provided email doesn't exist
            println("User with email $newEmail does not exist. Locker update failed.")
            return@transaction false
        }
    }
}
