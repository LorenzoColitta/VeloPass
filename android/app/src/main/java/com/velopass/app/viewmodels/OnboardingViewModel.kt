package com.velopass.app.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.velopass.app.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user

    fun nextStep() {
        if (_currentStep.value < 6) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun updateEmail(email: String) {
        _user.value = _user.value.copy(email = email)
    }

    fun updateDisplayName(name: String) {
        _user.value = _user.value.copy(displayName = name)
    }

    fun updateNationality(nationality: String) {
        _user.value = _user.value.copy(nationality = nationality)
    }

    fun updateLegalResidence(code: String) {
        _user.value = _user.value.copy(legalResidenceCode = code)
    }

    fun setSameCountry(same: Boolean) {
        if (same) {
            _user.value = _user.value.copy(legalResidenceCode = _user.value.nationality)
        }
    }
}
