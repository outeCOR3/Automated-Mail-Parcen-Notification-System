package org.example.project.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient

@Composable
fun AdminLandingPage(
    username: String,
    onNavigateToHome: () -> Unit = {},
    onNavigateToLock: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onCreateUser: () -> Unit = {},
    onLogout: () -> Unit = {}
) {

    var menuExpanded by remember { mutableStateOf(false) }
    var showCreateUser by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(0) }
    val client = HttpClient()

    if (showCreateUser) {
        // Display the CreateUserScreen when showCreateUser is true
        CreateUserScreen(
            onCreateUser = { username, password ->
                // Handle create user logic
                showCreateUser = false // Close after creating user
            },
            onCancel = {
                showCreateUser = false // Close if canceled
            },
            client = client
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = { Text(text = "LAND LORD", color = Color.White) },
                backgroundColor = Color(0xFF78C2D1),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                showCreateUser = true // Trigger showing CreateUserScreen
                            }) {
                                Text("Create User")
                            }
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                onLogout()
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                }
            )

            // Centered "Hello, Admin"
            Box(
                modifier = Modifier.weight(1f).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Hello, Admin, $username!", fontSize = 24.sp)
            }

            // Bottom Navigation Bar
            BottomNavigation(backgroundColor = Color(0xFF78C2D1), contentColor = Color.White) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        onNavigateToHome()
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        onNavigateToLock()
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        onNavigateToNotifications()
                    }
                )
            }
        }
    }
}
