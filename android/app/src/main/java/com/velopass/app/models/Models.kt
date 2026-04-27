package com.velopass.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

@Serializable
data class Bike(
    val bikeId: String = "",
    val ownerId: String = "",
    val shortRegNumber: String = "",
    val fullRegNumber: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 2024,
    val frameColor: String = "",
    val frameType: String = "",
    val frameMaterial: String? = null,
    val serialNumber: String? = null,
    val description: String? = null,
    val photoUrls: List<String> = emptyList(),
    val purchaseDate: String? = null,
    val purchasePrice: Double? = null,
    val purchaseCurrency: String? = null,
    val pdfFormUrl: String? = null,
    val bikeIndexId: String? = null,
    val registeredAt: String = Instant.now().toString(),
    val updatedAt: String = Instant.now().toString()
)

@Entity(tableName = "bikes")
@Serializable
data class BikeEntity(
    @PrimaryKey val bikeId: String,
    val ownerId: String,
    val shortRegNumber: String,
    val fullRegNumber: String,
    val make: String,
    val model: String,
    val year: Int,
    val frameColor: String,
    val frameType: String,
    val frameMaterial: String?,
    val serialNumber: String?,
    val description: String?,
    val photoUrls: String, // JSON serialized list
    val purchaseDate: String?,
    val purchasePrice: Double?,
    val purchaseCurrency: String?,
    val pdfFormUrl: String?,
    val bikeIndexId: String?,
    val registeredAt: String,
    val updatedAt: String
)

data class MaintenanceItem(
    val id: String = "",
    val title: String = "",
    val dueDate: String = "",
    val status: String = "Upcoming"
)

data class User(
    val email: String = "",
    val displayName: String = "",
    val nationality: String = "",
    val nationalityCode: String = "",
    val legalResidenceCode: String = ""
)

data class RegistrationNumbers(
    val short: String,
    val full: String
)
