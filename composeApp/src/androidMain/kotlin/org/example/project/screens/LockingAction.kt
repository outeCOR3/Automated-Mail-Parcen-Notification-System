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
fun LockingAction(token: String) {
    var isLocked by remember { mutableStateOf(true) }
    val lockingActionService = remember { LockingActionService(HttpClient()) }

    // This will attempt to fetch the lock status when the token is available
    LaunchedEffect(token) {
        if (lockingActionService.getLockStatus(token)) {
            val lockers = lockingActionService.getLockers()
            // You can use the locker status to update your UI or isLocked state
            if (lockers != null && lockers.isNotEmpty()) {
                isLocked = lockers.first().isLocked // Example logic
            }
        } else {
            // Handle error (display message, etc.)
        }
    }

    // Existing animation and UI code follows...
    val rotation by animateFloatAsState(
        targetValue = if (isLocked) 0f else 360f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        )
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isLocked) 0f else -15f,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    val glow by animateFloatAsState(
        targetValue = if (isLocked) 0.2f else 0.8f,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isLocked) Color.White else Color(0xFF2E2E2E),
        animationSpec = tween(600)
    )

    val lockColor by animateColorAsState(
        targetValue = if (isLocked) Color(0xFF90CAF9) else Color(0xFFFFC107),
        animationSpec = tween(600)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock Icon",
                tint = lockColor,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(
                        rotationZ = rotation,
                        translationY = offsetY
                    )
                    .clickable { isLocked = !isLocked }
            )

            if (!isLocked) {
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
                text = if (isLocked) "Locked" else "Unlocked",
                fontSize = 28.sp,
                color = lockColor
            )
        }
    }
}



