package com.velopass.app.models

data class Bike(
    val id: String = "",
    val registrationNumber: String = "",
    val vosNumber: String = "",
    val name: String = "Specimen Bike 2024",
    val isActive: Boolean = true
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
    val legalResidenceCode: String = ""
)
