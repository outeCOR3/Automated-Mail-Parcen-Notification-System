package org.example.project.model

import kotlinx.serialization.Serializable



@Serializable
enum class Roles{
    Admin,User
}
@Serializable
data class Users(
    val username: String,
    val email: String,
    val password:String,
    val roles: Roles
)


@Serializable
data class RegisterRequest(val email: String, val password: String, val username: String )

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val role: String)



@Serializable
data class RegisterUserRequest(val email: String, val password: String,val username: String)

@Serializable
data class RegisterUserResponse(val token: String, val role: String, val message:String)

@Serializable
data class ResetPasswordRequest(val email: String, val newPassword: String, val confirmNewPassword: String)