package org.example.project.model
import User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime


class UserRepository {
    private fun resultRowToUser(row: ResultRow): Users = Users(
        email = row[User.email],
        password = row[User.passwordHash],
        roles = row[User.role]
    )

    fun getAllUsers(): List<Users> = transaction {
        println("Fetching all users...")
        val result = User.selectAll().map(::resultRowToUser)
        println("Fetched ${result.size} users")
        result
    }

    fun getUserByEmail(email: String): Users? = transaction {
        User.selectAll()
            .where { User.email eq email }
            .map { row ->
                Users(
                    email = row[User.email],
                    password = row[User.passwordHash],
                    roles = row[User.role]
                )
            }
            .singleOrNull()
    }


    // Hash the password before storing it
    fun addUser(user: Users): Boolean = transaction {
        val result = User.select(User.email).where { User.email eq user.email }.count() > 0

        println("addUser(${user.email}): Query: ${User.select ( User.email eq user.email )}")
        if (!result) {
            val currentTime = LocalDateTime.now().atZone(java.time.ZoneId.of("UTC")).toInstant()
            User.insert {
                it[username] = user.email
                it[email] = user.email
                it[passwordHash] = user.password
                it[role] = user.roles
                it[createdAt] = currentTime
            }
            println("User ${user.email} inserted")
            true
        } else {
            println("User ${user.email} already exists: $result")
            false
        }
    }

    fun deleteUser(email: String): Boolean = transaction {
        User.deleteWhere { User.email eq email } > 0
    }
}
