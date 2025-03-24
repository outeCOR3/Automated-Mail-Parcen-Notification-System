package org.example.project.screens

import UserListScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import org.example.project.R
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
    var showManageUsers by remember { mutableStateOf(false) }
    var adminUsername by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableIntStateOf(0) }
    var showModal by remember { mutableStateOf(false) }

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
            onCreateUser = { _, _, _ -> showCreateUser = false },
            onCancel = { showCreateUser = false },
            client = client
        )
    } else if (showManageUsers) {
        UserListScreen(token = token, client = client)
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(50.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "LANDLORD - ${adminUsername ?: "Loading..."}",
                                color = Color.Black,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                    actions = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Manage Users") },
                                    onClick = {
                                        menuExpanded = false
                                        selectedItem = 3 // Set selectedItem to 3 for UserListScreen
                                    },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Manage Users") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Create User") },
                                    onClick = {
                                        menuExpanded = false
                                        showCreateUser = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonAdd, // User with a plus icon
                                            contentDescription = "Create User"
                                        )
                                    }

                                )
                                DropdownMenuItem(
                                    text = { Text("Assign a Locker") },
                                    onClick = {
                                        menuExpanded = false
                                        showModal = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.lockers),
                                            contentDescription = "Locker Box",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
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
                NavigationBar(modifier = Modifier.height(50.dp), containerColor = Color.White) {
                    val items = listOf("Home", "Lock", "Notifications")
                    val icons = listOf(Icons.Default.Home, Icons.Default.Lock, Icons.Default.Notifications)

                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item, modifier = Modifier.padding(bottom = 1.dp)) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                            }
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
                        0 -> onNavigateToHome()
                        1 -> LockerListScreen(token =token,client = client)
                        2 -> onNavigateToNotifications()
                        3 -> UserListScreen(token = token, client = client)
                    }
                }
            }
        )
    }

    if (showModal) {
        ModalAssignLocker(onDismiss = { showModal = false })
    }
}
