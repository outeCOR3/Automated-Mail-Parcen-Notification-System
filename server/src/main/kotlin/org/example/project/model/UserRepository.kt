package org.example.project.model
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime


class UserRepository {
    fun getAllUsers(): List<Users> = transaction {
        User.selectAll().map {
            Users(
                email = it[User.email],
                password = it[User.passwordHash],
                roles = Roles.valueOf(it[User.role])
            )
        }
    }

    fun getUserByEmail(email: String): Users? = transaction {
        User.select(User.email eq email) //
            .map {
                Users(
                    email = it[User.email],
                    password = it[User.passwordHash],
                    roles = Roles.valueOf(it[User.role])
                )
            }
            .singleOrNull()
    }

    // Hash the password before storing it
    fun addUser(user: Users): Boolean = transaction {
        // Check if the user already exists based on email
        if (User.select(User.email eq user.email).empty()) {
            val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
            val currentTime = LocalDateTime.now()

            User.insert {
                it[username] = user.email
                it[email] = user.email
                it[passwordHash] = hashedPassword
                it[role] = user.roles.name
                it[createdAt] = timestamp("created_at")
            }
            true
        } else {
            false
        }
    }


    fun deleteUser(email: String): Boolean = transaction {
        User.deleteWhere { User.email eq email } > 0 //
    }
    }

