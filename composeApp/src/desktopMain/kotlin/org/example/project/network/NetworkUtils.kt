package org.example.project.network


import java.net.InetAddress
import java.net.NetworkInterface

actual fun getLocalIpAddress(): String {
    return try {
        NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is InetAddress && !it.isLoopbackAddress && it.hostAddress.indexOf(':') == -1 }
            ?.hostAddress ?: "127.0.0.1"
    } catch (e: Exception) {
        "127.0.0.1"
    }
}
