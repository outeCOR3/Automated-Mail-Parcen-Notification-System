package org.example.project.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import org.example.project.service.LockingActionService

@Composable
fun LockingAction(token: String, client: HttpClient) {
    val lockingService = LockingActionService(client)
    val coroutineScope = rememberCoroutineScope()

    var isLocked by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch lock state initially
    LaunchedEffect(Unit) {
        val state = lockingService.getLockerState(token)
        isLocked = state ?: true // Default to locked if fetch fails
        errorMessage = lockingService.getErrorMessage()
    }

    if (isLocked == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", fontSize = 20.sp, color = Color.Gray)
        }
        return
    }

    // Animations
    val rotation by animateFloatAsState(
        targetValue = if (isLocked == true) 0f else 360f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "Rotation"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isLocked == true) 0f else -15f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "Floating Effect"
    )

    val glow by animateFloatAsState(
        targetValue = if (isLocked == true) 0.2f else 0.8f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "Glow Effect"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isLocked == true) Color.White else Color(0xFF2E2E2E),
        animationSpec = tween(600),
        label = "Background Shift"
    )

    val lockColor by animateColorAsState(
        targetValue = if (isLocked == true) Color.Black else Color.White,
        animationSpec = tween(600),
        label = "Lock Color"
    )

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isLocked == true) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock Icon",
                tint = lockColor,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(rotationZ = rotation, translationY = offsetY)
                    .clickable {
                        coroutineScope.launch {
                            val success = lockingService.toggleLockerState(token)
                            if (success) {
                                isLocked = isLocked?.not()
                            }
                            errorMessage = lockingService.getErrorMessage()
                        }
                    }
            )

            if (isLocked == false) {
                Canvas(modifier = Modifier.size(250.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(lockColor.copy(alpha = glow), Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width / 2
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLocked == true) "Locked" else "Unlocked",
                fontSize = 28.sp,
                color = lockColor
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, fontSize = 16.sp, color = Color.Red)
            }
        }
    }
}
