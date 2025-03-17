package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class LockingAction(
    val id: Int,  // References lockerId
    var isLocked: Boolean,

)