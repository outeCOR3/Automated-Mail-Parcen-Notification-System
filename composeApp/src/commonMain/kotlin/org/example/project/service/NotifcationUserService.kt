package org.example.project.service

import NotificationDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.project.model.Lockers

import org.example.project.model.LockingAction

class NotificationService(private val client: HttpClient) {
    private var errorMessage: String? = null
    private var userId: Int? = null

    suspend fun getUserId(token: String): Boolean {
        errorMessage = null

        if (token.isBlank()) {
            errorMessage = "Token cannot be empty."
            return false
        }

        return try {
            val response: HttpResponse = client.get("http://172.20.10.14:8080/locker/me") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status != HttpStatusCode.OK) {
                errorMessage = "Failed to retrieve user ID. Status: ${response.status}"
                return false
            }

            val json = response.body<String>()
            val lockerList: List<Lockers> = Json { ignoreUnknownKeys = true }
                .decodeFromString(json)

            userId = lockerList.firstOrNull()?.userId
            true
        } catch (e: Exception) {
            errorMessage = "Error retrieving user ID: ${e.localizedMessage}"
            false
        }
    }

    suspend fun getNotifications(token: String): List<NotificationDTO> {
        if (!getUserId(token) || userId == null) {
            errorMessage = "Failed to extract userId from /locker/me"
            return emptyList()
        }

        return try {
            val response: HttpResponse = client.get("http://172.20.10.14:8080/locker/notifications/$userId") {
                headers {
                    append("Authorization", "Bearer $token")
                    append("Accept", "application/json")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                Json { ignoreUnknownKeys = true }.decodeFromString(response.body())
            } else {
                errorMessage = "Failed to fetch notifications. Status: ${response.status}"
                emptyList()
            }
        } catch (e: Exception) {
            errorMessage = "Error fetching notifications: ${e.localizedMessage}"
            emptyList()
        }
    }

    fun getErrorMessage(): String? = errorMessage
}
