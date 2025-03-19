package org.example.project.network


import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    /*val localIp = getLocalIpAddress() ?: "127.0.0.1" // Default to localhost if no IP is found*/

    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                isLenient = true
                coerceInputValues = true
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "192.168.68.138"
                port = 8080
            }
        }
    }
}