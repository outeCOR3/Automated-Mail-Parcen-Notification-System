package org.example.project.model
import User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.time.ZoneId


class UserRepository {
    fun resultRowToUser(row: ResultRow): Users = Users(
        email = row[User.email],
        password = row[User.passwordHash],
        roles = row[User.role],
        username = row[User.username]
    )

    fun getAllUsers(): List<Users> = transaction {
        println("Fetching all users...")
        val result = User.selectAll().map(::resultRowToUser)
        println("Fetched ${result.size} users")
        result
    }

    fun getUserByRole(role: Roles): List<Users> = transaction {
        println("Fetching users with role $role...")
        val result = User.selectAll().where { User.role eq role }.map(::resultRowToUser)
        println("Fetched ${result.size} users with role $role")
        result
    }


    fun getUserByEmail(email: String): Users? = transaction {
        User.selectAll()
            .where { User.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    fun getUserByUsername(username: String): Users? = transaction {
        User.selectAll()
            .where { User.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()  // ✅ Move inside transaction block

    }


    // Hash the password before storing it
    fun addUser(user: Users): Boolean = transaction {
        val exists = User.select(User.email).where { User.email eq user.email }.count() > 0

        println("addUser(${user.email}): Checking if user exists...")
        if (!exists) {
            val currentTime =
                LocalDateTime.now().atZone(ZoneId.of("Asia/Manila")).toInstant() // Convert to UTC+8
            User.insert {
                it[username] = user.username  // Store username separately
                it[email] = user.email
                it[passwordHash] = user.password // Hash password
                it[role] = user.roles
                it[createdAt] = currentTime
            }
            println("User ${user.email} inserted with username ${user.username}")
            true
        } else {
            println("User ${user.email} already exists")
            false
        }
    }


    fun deleteUser(email: String): Boolean = transaction {
        User.deleteWhere { User.email eq email } > 0
    }

    fun resetPassword(email: String, newPassword: String, confirmNewPassword: String): Boolean =
        transaction {
            val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12))

            val updatedRows = User.update({ User.email eq email }) {
                it[passwordHash] = hashedPassword

            }
            updatedRows > 0
        }

    fun getUserById(id: Int): Users? = transaction {
        User.selectAll()
            .where { User.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }
}


      // Returns true if at least one row was updated


