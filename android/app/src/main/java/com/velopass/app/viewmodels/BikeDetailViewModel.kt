package com.velopass.app.viewmodels

import androidx.lifecycle.ViewModel
import com.velopass.app.models.Bike
import com.velopass.app.data.BikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BikeDetailViewModel @Inject constructor(
    private val bikeRepository: BikeRepository
) : ViewModel() {

    fun getBike(bikeId: String): Flow<Bike?> {
        return kotlinx.coroutines.flow.flowOf()
    }

    suspend fun updateBike(bike: Bike) {
        bikeRepository.updateBike(bike)
    }

    suspend fun deleteBike(bikeId: String) {
        bikeRepository.deleteBike(bikeId)
    }
}
