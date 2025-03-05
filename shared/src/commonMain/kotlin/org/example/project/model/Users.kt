package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
enum class Roles{
    Admin,User
}
@Serializable
data class Users(
    val email: String,
    val password:String,
    val roles: Roles
)
