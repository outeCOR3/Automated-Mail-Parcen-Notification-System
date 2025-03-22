package org.example.project.screens

import UserListScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import org.example.project.model.UsersDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLandingPage(
    token: String,
    client: HttpClient,
    onNavigateToHome: () -> Unit = {},
    onNavigateToLock: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onCreateUser: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showCreateUser by remember { mutableStateOf(false) }
    var adminUsername by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val response: HttpResponse = client.get("http://192.168.8.132:8080/users/me") {
                header("Authorization", "Bearer $token")
            }
            if (response.status == io.ktor.http.HttpStatusCode.OK) {
                val user = Json { ignoreUnknownKeys = true }.decodeFromString<UsersDTO>(response.body())

                adminUsername = user.username
            } else {
                adminUsername = "Error: ${response.status}"
            }
        } catch (e: Exception) {
            adminUsername = "Error: ${e.message}"
        }
    }

    if (showCreateUser) {
        CreateUserScreen(
            onCreateUser = {username, email, password   -> showCreateUser = false },
            onCancel = { showCreateUser = false },
            client = client
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("LANDLORD - ${adminUsername ?: "Loading..."}", color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF42A5F5)),
                    actions = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Create User") },
                                    onClick = {
                                        menuExpanded = false
                                        showCreateUser = true
                                    },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Profile") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        menuExpanded = false
                                        onBackToLogin()
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") }
                                )
                            }
                        }
                    }
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFBBDEFB)),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserListScreen(client = client, token = token)
            }
        }
    }
}
