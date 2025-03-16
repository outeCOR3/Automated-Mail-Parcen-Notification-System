package org.example.project.model


import kotlinx.serialization.Serializable

@Serializable
data class Lockers(
    val id: Int? = null,
        val sensorId: String,
        val username: String,
    )
