package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Composable
fun LockerListScreen(token: String, client: HttpClient) {
    var lockers by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response: HttpResponse = client.get("http://172.20.10.14:8080/users/lockerAll/status") {
                    header("Authorization", "Bearer $token")
                }
                if (response.status == HttpStatusCode.OK) {
                    val jsonText = response.bodyAsText()
                    println("API Response: $jsonText")

                    val lockerStatuses = Json.decodeFromString<List<Boolean>>(jsonText)
                    lockers = lockerStatuses.mapIndexed { index, isLocked ->
                        "Locker ${index + 1}: ${if (isLocked) "Locked" else "Unlocked"}"
                    }
                } else {
                    errorMessage = "Failed to fetch lockers: ${response.status}"
                }
            } catch (e: Exception) {
                errorMessage = "Error fetching lockers: ${e.message}"
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 18.sp)
        } else if (lockers.isEmpty()) {
            CircularProgressIndicator()
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(lockers) { lockerName ->
                    LockerCard(lockerName = lockerName)
                }
            }
        }
    }
}

@Composable
fun LockerCard(lockerName: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = lockerName, fontSize = 20.sp, color = Color.Black)
        }
    }
}
