package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import org.example.project.model.UsersDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLandingPage(client: HttpClient) {
    var selectedItem by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf<String?>(null) }

    // Fetch user data
    LaunchedEffect(Unit) {
        try {
            val response: HttpResponse = client.get("http://192.168.68.138:8080//users/role/User")
            val responseBody = response.body<String>()
            username = Json.decodeFromString<UsersDTO>(responseBody).username
        } catch (e: Exception) {
            println("Error fetching username: ${e.message}")
            username = "Error"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = username ?: "Loading...", fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            onClick = { showMenu = false },
                            text = { Text("Profile") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
                        )
                        DropdownMenuItem(
                            onClick = { showMenu = false },
                            text = { Text("Logout") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF42A5F5))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF42A5F5)) {
                val items = listOf("Home", "Lock", "Notifications")
                val icons = listOf(Icons.Default.Home, Icons.Default.Lock, Icons.Default.Notifications)

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
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
                    .background(Color(0xFFBBDEFB)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Hello, ${username ?: "Loading..."}!", fontSize = 24.sp)
            }
        }
    )
}
