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
        roles = row[User.role],
        username = row[User.username]
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
                    roles = row[User.role],
                    username = row[User.username]
                )
            }
            .singleOrNull()
    }
    fun getUserByUsername(username: String): Users? = transaction {
        User.selectAll()
            .where { User.username eq username }
            .map { row ->
                Users(
                    username = row[User.username],
                    email = row[User.email],
                    password = row[User.passwordHash],
                    roles = row[User.role]
                )
            }
            .singleOrNull()
    }

    // Hash the password before storing it
    fun addUser(user: Users): Boolean = transaction {
        val exists = User.select(User.email).where { User.email eq user.email }.count() > 0

        println("addUser(${user.email}): Checking if user exists...")
        if (!exists) {
            val currentTime = LocalDateTime.now().atZone(java.time.ZoneId.of("UTC")).toInstant()
            User.insert {
                it[username] = user.username  // Store username separately
                it[email] = user.email
                it[passwordHash] = BCrypt.hashpw(user.password, BCrypt.gensalt()) // Hash password
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
}
