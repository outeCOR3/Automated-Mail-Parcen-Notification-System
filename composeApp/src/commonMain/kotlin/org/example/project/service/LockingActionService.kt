package org.example.project.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.project.model.LockingAction

class LockingActionService(private val client: HttpClient) {
    private var errorMessage: String? = null
    private var lockerId: Int? = null

    suspend fun toggleLockerState(token: String): Boolean {
        errorMessage = null

        if (token.isBlank()) {
            errorMessage = "Token cannot be empty."
            return false
        }

        return try {
            // Step 1: Get locker info
            val response: HttpResponse = client.get("http://192.168.8.132:8080/locker/me") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status != HttpStatusCode.OK) {
                errorMessage = "Failed to retrieve user info. Status: ${response.status}"
                return false
            }

            val lockers: List<LockingAction> = Json { ignoreUnknownKeys = true }
                .decodeFromString(response.body())

            val locker = lockers.firstOrNull()
            if (locker == null) {
                errorMessage = "No locker found for the given token."
                return false
            }

            lockerId = locker.id
            val newLockState = !locker.isLocked  // Toggle the current state

            // Step 2: Send updated lock state
            val lockResponse: HttpResponse = client.post("http://192.168.8.132:8080/locker/lockers/lock") {
                headers {
                    append("Authorization", "Bearer $token")
                    append("Content-Type", "application/json")
                }
                setBody(Json.encodeToString(LockingAction.serializer(), LockingAction(id = lockerId!!, isLocked = newLockState)))
            }

            if (lockResponse.status == HttpStatusCode.OK) {
                true  // Successfully toggled
            } else {
                errorMessage = "Failed to change locker state. Status: ${lockResponse.status}"
                false
            }
        } catch (e: Exception) {
            errorMessage = "Error changing locker state: ${e.localizedMessage}"
            false
        }
    }

fun getErrorMessage(): String? = errorMessage
}

