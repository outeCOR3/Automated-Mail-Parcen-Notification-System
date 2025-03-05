package org.example.project.model

interface UserRepository  {
    fun allUsers(): List<Users>
    fun userByEmail(email: String): Users?
    fun usersByRole(role: Roles): List<Users>
    fun addOrUpdateUser(user: Users)
    fun removeUser(email: String): Boolean
}
