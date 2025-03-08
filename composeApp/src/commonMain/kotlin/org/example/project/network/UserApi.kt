package org.example.project.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.model.Users

class UserApi(private val httpClient: HttpClient) {

    suspend fun getAllUsers(): List<Users> {
        return httpClient.get("http://192.168.8.132:8080/users").body()  // Fetch users from backend
    }

    suspend fun getUserByEmail(email: String): Users? {
        return httpClient.get("http://192.168.8.132:8080/users/$email").body()  // Fetch a user by email
    }

    suspend fun getUsersByRole(role: String): List<Users> {
        return httpClient.get("http://192.168.8.132:8080//users/byRole/$role").body()  // Fetch users by role
    }

    suspend fun addOrUpdateUser(user: Users) {
        httpClient.post("http://192.168.8.132:8080/users") {
            contentType(ContentType.Application.Json)
            setBody(user)  // Save the user to the backend
        }
    }

    suspend fun removeUser(email: String) {
        httpClient.delete("http://192.168.8.132:8080/users/$email")  // Delete user from the backend
    }
}
