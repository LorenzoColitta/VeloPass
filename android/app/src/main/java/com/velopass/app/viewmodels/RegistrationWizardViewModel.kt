package com.velopass.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velopass.app.models.Bike
import com.velopass.app.models.User
import com.velopass.app.data.BikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WizardState(
    val currentStep: Int = 1,
    val make: String = "",
    val model: String = "",
    val year: Int = 2024,
    val frameType: String = "",
    val frameColor: String = "",
    val frameMaterial: String = "",
    val serialNumber: String = "",
    val photos: List<String> = emptyList(),
    val purchaseDate: String? = null,
    val purchasePrice: Double? = null,
    val purchaseCurrency: String = "EUR",
    val description: String = "",
    val bikeIndexConnected: Boolean = false,
    val bikeIndexUsername: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RegistrationWizardViewModel @Inject constructor(
    private val bikeRepository: BikeRepository
) : ViewModel() {

    private val _wizardState = MutableStateFlow(WizardState())
    val wizardState: StateFlow<WizardState> = _wizardState.asStateFlow()

    fun updateStep(step: Int) {
        _wizardState.value = _wizardState.value.copy(currentStep = step)
    }

    fun updateBasicInfo(make: String, model: String, year: Int, frameType: String) {
        _wizardState.value = _wizardState.value.copy(
            make = make.take(60),
            model = model.take(80),
            year = year,
            frameType = frameType
        )
    }

    fun updateFrameDetails(color: String, material: String, serial: String) {
        _wizardState.value = _wizardState.value.copy(
            frameColor = color,
            frameMaterial = material,
            serialNumber = serial.ifEmpty { null }
        )
    }

    fun addPhoto(photoUri: String) {
        val current = _wizardState.value.photos
        if (current.size < 8) {
            _wizardState.value = _wizardState.value.copy(
                photos = current + photoUri
            )
        }
    }

    fun removePhoto(index: Int) {
        val current = _wizardState.value.photos
        _wizardState.value = _wizardState.value.copy(
            photos = current.filterIndexed { i, _ -> i != index }
        )
    }

    fun updatePurchaseInfo(date: String?, price: Double?, currency: String) {
        _wizardState.value = _wizardState.value.copy(
            purchaseDate = date,
            purchasePrice = price,
            purchaseCurrency = currency
        )
    }

    fun updateDescription(text: String) {
        _wizardState.value = _wizardState.value.copy(
            description = text.take(500)
        )
    }

    fun toggleBikeIndex() {
        _wizardState.value = _wizardState.value.copy(
            bikeIndexConnected = !_wizardState.value.bikeIndexConnected
        )
    }

    fun setBikeIndexUsername(username: String) {
        _wizardState.value = _wizardState.value.copy(
            bikeIndexUsername = username,
            bikeIndexConnected = true
        )
    }

    fun confirmRegistration(userId: String, nationalityCode: String, legalResidenceCode: String) {
        viewModelScope.launch {
            _wizardState.value = _wizardState.value.copy(isLoading = true, errorMessage = null)
            try {
                val state = _wizardState.value
                val bikeId = UUID.randomUUID().toString()

                val bike = Bike(
                    bikeId = bikeId,
                    ownerId = userId,
                    make = state.make,
                    model = state.model,
                    year = state.year,
                    frameColor = state.frameColor,
                    frameType = state.frameType,
                    frameMaterial = state.frameMaterial.ifEmpty { null },
                    serialNumber = state.serialNumber,
                    description = state.description.ifEmpty { null },
                    photoUrls = state.photos,
                    purchaseDate = state.purchaseDate,
                    purchasePrice = state.purchasePrice,
                    purchaseCurrency = state.purchaseCurrency,
                    bikeIndexId = if (state.bikeIndexConnected) state.bikeIndexUsername else null
                )

                val registeredBike = bikeRepository.registerBike(
                    bike,
                    userId,
                    nationalityCode,
                    legalResidenceCode
                )

                _wizardState.value = _wizardState.value.copy(
                    isLoading = false,
                    currentStep = 8 // Success state
                )
            } catch (e: Exception) {
                _wizardState.value = _wizardState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun validateStep(step: Int): Boolean {
        val state = _wizardState.value
        return when (step) {
            1 -> state.make.isNotEmpty() && state.model.isNotEmpty() && state.frameType.isNotEmpty()
            2 -> state.frameColor.isNotEmpty() && state.frameMaterial.isNotEmpty()
            3 -> state.photos.isNotEmpty()
            4 -> true // All optional
            5 -> true // Optional
            6 -> true // Optional
            7 -> true // Review step
            else -> false
        }
    }
}
