package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class Parcels(
    val id: Int? = null,              // Optional, auto-incremented by DB
    val trackingNumber: String, // Nullable, but we’ll validate it’s not blank in the route
    val imageUrl: String? = null,     // Nullable, as per schema
 // Added to match table schema
)