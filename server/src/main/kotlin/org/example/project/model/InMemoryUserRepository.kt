/*package org.example.project.model


class InMemoryUserRepository: UserRepository {

        private var users = listOf(
            Users("admin@example.com", "securepass", Roles.Admin),
            Users("user1@example.com", "password123", Roles.org.example.project.model.User),
            Users("user2@example.com", "mypassword", Roles.org.example.project.model.User),
            Users("moderator@example.com", "modpass", Roles.Admin)
        )

        override fun allUsers(): List<Users> = users

        override fun userByEmail(email: String): Users? = users.find {
            it.email.equals(email, ignoreCase = true)
        }

        override fun usersByRole(role: Roles): List<Users> = users.filter {
            it.roles == role
        }

        override fun addOrUpdateUser(user: Users) {
            var notFound = true

            users = users.map {
                if (it.email.equals(user.email, ignoreCase = true)) {
                    notFound = false
                    user
                } else {
                    it
                }
            }
            if (notFound) {
                users = users.plus(user)
            }
        }

        override fun removeUser(email: String): Boolean {
            val oldUsers = users
            users = users.filterNot { it.email.equals(email, ignoreCase = true) }
            return oldUsers.size > users.size
        }
    }

*/