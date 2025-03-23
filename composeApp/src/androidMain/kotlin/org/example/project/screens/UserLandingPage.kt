package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import org.example.project.model.UsersDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLandingPage(client: HttpClient, token: String,onBackToLogin: () -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf<String?>(null) }

    // Fetch user data
    LaunchedEffect(Unit) {
        try {
            val response: HttpResponse = client.get("http://192.168.8.132:8080/users/me") {
                header("Authorization", "Bearer $token")
            }
            if (response.status == HttpStatusCode.OK) {
                val user = Json.decodeFromString<UsersDTO>(response.body())
                username = user.username
            } else {
                username = "Request Failed: ${response.status}"
            }
        } catch (e: Exception) {
            username = "Error: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = username ?: "Loading...",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                        },

                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = { showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                onBackToLogin()}, // Add logout logic
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF42A5F5))
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(50.dp),containerColor = Color(0xFF42A5F5)) {

                val items = listOf("Home", "Lock", "Notifications")
                val icons = listOf(Icons.Default.Home, Icons.Default.Lock, Icons.Default.Notifications)

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item,modifier = Modifier
                             // Reduce icon size
                            .padding(bottom = 1.dp)) },

                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                when (selectedItem) {
                    0 -> Text(text = "Hello, ${username ?: "Loading..."}!", fontSize = 24.sp)
                    1 -> LockingAction(token = token, client = client)
                    2 -> NotificationScreenUser(token = token, client = client) // Now fetches locker ID dynamically
                }
            }
        }
    )
}