package org.example.project.network

import java.net.NetworkInterface

actual fun getLocalIpAddress(): String {
    return try {
        val ipAddress = NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { inetAddress ->
                inetAddress.hostAddress?.let { address ->
                    !inetAddress.isLoopbackAddress && address.indexOf(':') == -1
                } ?: false
            }?.hostAddress

        ipAddress ?: "192.168.8.132" // Return fallback if null
    } catch (e: Exception) {
        "192.168.8.132" // Return fallback on exception
    }
}
