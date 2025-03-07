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
        return httpClient.get("users").body()
    }

    suspend fun getUserByEmail(email: String): Users? {
        return httpClient.get("users/$email").body()
    }

    suspend fun getUsersByRole(role: String): List<Users> {
        return httpClient.get("users/byRole/$role").body()
    }

    suspend fun addOrUpdateUser(user: Users) {
        httpClient.post("users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
    }

    suspend fun removeUser(email: String) {
        httpClient.delete("users/$email")
    }
}
