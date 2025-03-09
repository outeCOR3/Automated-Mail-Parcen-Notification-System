package org.example.project.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.BottomAppBar
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

    var isHomeIconClicked by remember { mutableStateOf(false) }
    var isLockIconClicked by remember { mutableStateOf(false) }
    var isNotificationIconClicked by remember { mutableStateOf(false) }
    val client = HttpClient()

    if (showCreateUser) {
        CreateUserScreen(
            onCreateUser = { username, password ->
                showCreateUser = false
                // Handle user creation logic
            },
            onCancel = { showCreateUser = false },
            client = client
        )
    } else {
        val bottomBarHeight = 70.dp
        val bottomBarShape = CutCornerShape(topStart = 24.dp, topEnd = 24.dp)

        // Animation configurations
        val animationSpec = tween<Dp>(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing)

        // Home icon animations
        val homeIconSize by animateDpAsState(targetValue = if (isHomeIconClicked) 72.dp else 60.dp, animationSpec = animationSpec)
        val homeIconOffset by animateDpAsState(targetValue = if (isHomeIconClicked) (-40).dp else (9).dp, animationSpec = animationSpec)

        // Lock icon animations
        val lockIconSize by animateDpAsState(targetValue = if (isLockIconClicked) 72.dp else 60.dp, animationSpec = animationSpec)
        val lockIconOffset by animateDpAsState(targetValue = if (isLockIconClicked) (-40).dp else(9).dp, animationSpec = animationSpec)

        // Notification icon animations
        val notificationIconSize by animateDpAsState(targetValue = if (isNotificationIconClicked) 72.dp else 60.dp, animationSpec = animationSpec)
        val notificationIconOffset by animateDpAsState(targetValue = if (isNotificationIconClicked) (-40).dp else (9).dp, animationSpec = animationSpec)

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(text = "LAND LORD", color = Color.White) },
                backgroundColor = Color(0xFF78C2D1),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                showCreateUser = true
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

            Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Hello, Admin, $username!", fontSize = 24.sp)
            }

            // Main screen content area
            Box(modifier = Modifier.fillMaxWidth()) {
                // Overlapping icons with animations outside the BottomAppBar
                Box(
                    modifier = Modifier
                        .offset(y = homeIconOffset)  // Control home icon's vertical offset
                        .align(Alignment.BottomStart)  // Align to the bottom left of the screen
                        .padding(start = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(homeIconSize)
                            .background(Color(0xFF5DA8B8), shape = CircleShape)
                            .clickable {
                                isHomeIconClicked = !isHomeIconClicked
                                selectedItem = 0
                                onNavigateToHome()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = lockIconOffset)  // Control lock icon's vertical offset
                        .align(Alignment.BottomCenter)  // Align to the bottom center of the screen
                ) {
                    Box(
                        modifier = Modifier
                            .size(lockIconSize)
                            .background(Color(0xFF5DA8B8), shape = CircleShape)
                            .clickable {
                                isLockIconClicked = !isLockIconClicked
                                selectedItem = 1
                                onNavigateToLock()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Lock",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = notificationIconOffset)  // Control notification icon's vertical offset
                        .align(Alignment.BottomEnd)  // Align to the bottom right of the screen
                        .padding(end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(notificationIconSize)
                            .background(Color(0xFF5DA8B8), shape = CircleShape)
                            .clickable {
                                isNotificationIconClicked = !isNotificationIconClicked
                                selectedItem = 2
                                onNavigateToNotifications()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            // Bottom app bar without space between the app bar and icons
            BottomAppBar(
                backgroundColor = Color(0xFF78C2D1),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight) // Keep the height for the BottomAppBar
                    .padding(0.dp), // No padding
                elevation = 12.dp
            ) {
                // BottomAppBar items can be added here, but it's empty for now.
                Spacer(modifier = Modifier.weight(1f))
                // You can add more actions here like icons or buttons if needed.
            }
        }
    }
}
