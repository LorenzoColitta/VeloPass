package com.velopass.app.models

import kotlinx.serialization.Serializable

@Serializable
data class BikeIndexProfile(
    val username: String,
    val email: String,
    val bikeIndexId: Long
)

@Serializable
data class BikeIndexResult(
    val bikeIndexId: Long,
    val url: String
)

@Serializable
data class BikeIndexSearchResult(
    val id: Long,
    val title: String,
    val brand: String,
    val model: String,
    val year: Int,
    val serial: String?,
    val color: String?,
    val url: String
)

enum class BikeIndexConnectionState {
    UNKNOWN,
    NOT_CONNECTED,
    CONNECTED,
    EXPIRED,
    LOADING,
    ERROR
}
