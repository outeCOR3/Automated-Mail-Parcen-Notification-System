package org.example.project.utils

import java.net.InetAddress
import java.net.NetworkInterface

fun getLocalIpAddress(): String? {
    return NetworkInterface.getNetworkInterfaces().toList()
        .flatMap { it.inetAddresses.toList() }
        .filter { it is InetAddress && !it.isLoopbackAddress && it.hostAddress.contains('.') } // Only IPv4
        .map { it.hostAddress }
        .firstOrNull() // Take the first valid IP or return null
}
