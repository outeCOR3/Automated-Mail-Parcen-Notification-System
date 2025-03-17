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
            .select(Locker.id, User.username)
            .map(::resultRowToLocker)
    }

    fun getLockersByUsername(username: String): List<Lockers> = transaction {
        (Locker innerJoin User)
            .select(Locker.id, User.username)
            .where { User.username eq username }
            .map(::resultRowToLocker)
    }

    fun addLocker(username: String): Boolean = transaction {
        println("Checking if user with username $username exists...")
        val user = userRepository.getUserByUsername(username) ?: return@transaction false

        val existingLocker =
            Locker.select(Locker.username).where { Locker.username eq username }.singleOrNull()
        if (existingLocker != null) {
            println("Locker already exists for user: $username")
            return@transaction false
        }

        val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila"))
        Locker.insert {
            it[Locker.username] = username
            it[createdAt] = phTime.toInstant()
        }

        println("Added locker for user: $username at $phTime (UTC+8)")
        true
    }

    fun deleteLocker(id: Int): Boolean = transaction {
        Locker.deleteWhere { Locker.id eq id } > 0
    }

    // Ensure this method is returning a nullable User


    fun updateLocker(id: Int, newEmail: String): Boolean = transaction {
        // Fetch the user based on the new email provided
        val user = userRepository.getUserByEmail(newEmail)

        // Check if the user exists
        if (user != null) {
            // Extract the username from the found user
            val newUsername = user.username

            // Check if the username is already assigned to another locker
            val existingLocker = Locker.select(Locker.username)
                .where ( Locker.username eq newUsername and (Locker.id neq id))
                .singleOrNull()

            if (existingLocker != null) {
                println("Username $newUsername is already assigned to another locker.")
                return@transaction false
            }

            // Proceed with updating the locker with the new username
            val updatedRows = Locker.update({ Locker.id eq id }) {
                it[username] = newUsername  // Set the new username from the user
            }

            // Log the successful update
            println("Updated locker with ID: $id to new username: $newUsername (from email: $newEmail)")

            // Return true if the update was successful (i.e., at least one row was updated)
            return@transaction updatedRows > 0
        } else {
            // If the user with the provided email doesn't exist
            println("User with email $newEmail does not exist. Locker update failed.")
            return@transaction false
        }
    }
}
