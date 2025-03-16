package org.example.project.utils

import java.net.InetAddress

fun getLocalHostname(): String {
    return InetAddress.getLocalHost().hostName
}

