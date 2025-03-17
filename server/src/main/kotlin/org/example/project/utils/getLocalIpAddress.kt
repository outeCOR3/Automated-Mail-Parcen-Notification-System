package org.example.project.utils

import java.net.Inet4Address
import java.net.NetworkInterface

fun getLocalIpAddress(): String {
    return try {
        NetworkInterface.getNetworkInterfaces().toList()
            .asSequence() // Convert to sequence for efficient filtering
            .filter { it.isUp && !it.isLoopback } // Ignore down and loopback interfaces
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>() // Only IPv4 addresses
            .map { it.hostAddress }
            .firstOrNull() ?: "192.168.8.132" // Default if no IP found
    } catch (e: Exception) {
        "0.0.0.0" // Fallback to a safe IP
    }
}
