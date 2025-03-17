package org.example.project.model

import User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.ZoneId

class LockerRepository(private val userRepository: UserRepository) {

    private fun resultRowToLocker(row: ResultRow): Lockers = Lockers(
        id = row[Locker.id],
        userId = row[Locker.user_id],  // Changed to match Lockers class
        lockerId = row[Locker.locker_id] // Changed to match Lockers class
    )


    fun getAllLockers(): List<Lockers> = transaction {
        println("Fetching all lockers with usernames from User table...")
        (Locker innerJoin User)
            .selectAll()
            .map(::resultRowToLocker)
    }

    fun getLockersById(id: Int): List<Lockers> = transaction {
        Locker.select(Locker.id)
            .where{ Locker.id eq id }
            .map(::resultRowToLocker)
    }

    fun addLocker(userId: Int): Boolean = transaction {
        println("Checking if user with id: $userId exists...")
        val user = userRepository.getUserById(userId) ?: return@transaction false

        val existingLocker = Locker.select(Locker.id) .where{ Locker.user_id eq userId }.singleOrNull()
        if (existingLocker != null) {
            println("Locker already exists for user: $userId")
            return@transaction false
        }

        val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()
        Locker.insert {
            it[Locker.user_id] = userId
            it[Locker.createdAt] = phTime
        }

        println("Added locker for user: $userId at $phTime (UTC+8)")
        true
    }

    fun deleteLocker(id: Int): Boolean = transaction {
        Locker.deleteWhere { Locker.id eq id } > 0
    }

    fun updateLocker(id: Int, newUserId: Int): Boolean = transaction {
        val user = userRepository.getUserById(newUserId) ?: return@transaction false

        val existingLocker = Locker.select (Locker.id).where{ Locker.user_id eq newUserId and (Locker.id neq id) }.singleOrNull()
        if (existingLocker != null) {
            println("User $newUserId is already assigned to another locker.")
            return@transaction false
        }

        val updatedRows = Locker.update({ Locker.id eq id }) {
            it[Locker.user_id] = newUserId
        }

        println("Updated locker with ID: $id to new user ID: $newUserId")
        updatedRows > 0
    }
}