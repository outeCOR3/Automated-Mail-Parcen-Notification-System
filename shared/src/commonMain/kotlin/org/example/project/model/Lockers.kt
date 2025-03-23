package org.example.project.model



import kotlinx.datetime.Instant


import kotlinx.serialization.Serializable

@Serializable
data class Lockers(
    val id: Int? = null,
    val userId: Int,
    val lockerId: Int? = null,
    val isLocked: Boolean,
    )

