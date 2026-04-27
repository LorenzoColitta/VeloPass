package com.velopass.app.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.velopass.app.models.Bike
import com.velopass.app.models.MaintenanceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _activeBike = MutableStateFlow(
        Bike(
            id = "1",
            registrationNumber = "AB-123-CD",
            vosNumber = "VOSNUM123456",
            name = "Specimen Bike 2024",
            isActive = true
        )
    )
    val activeBike: StateFlow<Bike> = _activeBike

    private val _maintenanceItems = MutableStateFlow(
        listOf(
            MaintenanceItem(
                id = "1",
                title = "Chain Lubrication",
                dueDate = "2024-05-15",
                status = "Upcoming"
            ),
            MaintenanceItem(
                id = "2",
                title = "Tire Pressure Check",
                dueDate = "2024-05-20",
                status = "Upcoming"
            )
        )
    )
    val maintenanceItems: StateFlow<List<MaintenanceItem>> = _maintenanceItems
}
