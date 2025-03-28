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
        lockerId = row[Locker.locker_id], // Changed to match Lockers class
        isLocked = row[Locker.isLocked]     // Ensure it's included
    )



    fun getAllLockers(): List<Lockers> = transaction {
        println("Fetching all lockers with usernames from User table...")
        (Locker innerJoin User)
            .selectAll()
            .map(::resultRowToLocker)
    }

    fun getLockersById(id: Int): List<Lockers?> = transaction {

        Locker.selectAll() // Changed from Locker.select(Locker.id) to select all columns
            .where { Locker.user_id eq id }
            .map(::resultRowToLocker)


    }

    fun addLocker(userId: Int,isLocked: Boolean): Boolean = transaction {
        println("Checking if user with id: $userId exists...")
        val user = userRepository.getUserById(userId) ?: return@transaction false

        val existingLocker =
            Locker.select(Locker.id).where { Locker.user_id eq userId }.singleOrNull()
        if (existingLocker != null) {
            println("Locker already exists for user: $userId")
            return@transaction false
        }

        val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()
        Locker.insert {
            it[Locker.isLocked] = isLocked
            it[user_id] = userId
            it[createdAt] = phTime
        }

        println("Added locker for user: $userId at $phTime (UTC+8)")
        true
    }

    fun deleteLocker(id: Int): Boolean = transaction {
        Locker.deleteWhere { Locker.id eq id } > 0
    }

    fun updateLocker(lockerId: Int, newUserId: Int): Boolean = transaction {
        val user = userRepository.getUserById(newUserId) ?: return@transaction false

        val existingLocker = Locker.select(Locker.locker_id)
            .where { Locker.user_id eq newUserId and (Locker.locker_id neq lockerId) }
            .singleOrNull()
        if (existingLocker != null) {
            println("User $newUserId is already assigned to another locker.")
            return@transaction false
        }

        val updatedRows = Locker.update({ Locker.locker_id eq lockerId }) {
            it[Locker.user_id] = newUserId
        }

        println("Updated locker with locker_id: $lockerId to new user ID: $newUserId")
        updatedRows > 0
    }

    fun updateLockerLockState(lockerId: Int, isLocked: Boolean): Boolean = transaction {
        val updatedRows = Locker.update({ Locker.locker_id eq lockerId }) {  // Use locker_id
            it[Locker.isLocked] = isLocked

        }
        updatedRows > 0  // Return true if update was successful
    }

    fun getLockersByLockerId(lockerId: Int): List<Lockers> = transaction {
        Locker.selectAll()
            .where { Locker.user_id eq lockerId }  // Ensure filtering by locker_id
            .map(::resultRowToLocker)  // Convert ResultRow to Locker object
    }
    fun getAllLockersStatus(): List<Boolean> = transaction {
        Locker.select(Locker.isLocked)
            .map { it[Locker.isLocked] }
    }


    fun getLockerByUserId(id: Int): List<Lockers?> = transaction {
        Locker.selectAll() // Changed from Locker.select(Locker.id) to select all columns
            .where { Locker.id eq id }
            .map(::resultRowToLocker)

    }
}