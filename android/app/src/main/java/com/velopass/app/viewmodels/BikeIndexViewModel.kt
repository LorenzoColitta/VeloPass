package com.velopass.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velopass.app.data.BikeIndexRepository
import com.velopass.app.models.BikeIndexConnectionState
import com.velopass.app.models.BikeIndexProfile
import com.velopass.app.models.BikeIndexSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BikeIndexViewModel @Inject constructor(
    private val bikeIndexRepository: BikeIndexRepository
) : ViewModel() {
    
    private val _connectionState = MutableStateFlow<BikeIndexConnectionState>(BikeIndexConnectionState.UNKNOWN)
    val connectionState: StateFlow<BikeIndexConnectionState> = _connectionState.asStateFlow()
    
    private val _profile = MutableStateFlow<BikeIndexProfile?>(null)
    val profile: StateFlow<BikeIndexProfile?> = _profile.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<BikeIndexSearchResult>>(emptyList())
    val searchResults: StateFlow<List<BikeIndexSearchResult>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Load connection state from stored preferences
        loadConnectionState()
    }
    
    fun handleOAuthCallback(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val profile = bikeIndexRepository.authorizeWithBikeIndex(code)
                _profile.value = profile
                _connectionState.value = BikeIndexConnectionState.CONNECTED
            } catch (e: Exception) {
                _error.value = e.message ?: "Authorization failed"
                _connectionState.value = BikeIndexConnectionState.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchBikeBySerial(serialNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val results = bikeIndexRepository.verifyBike(serialNumber)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun disconnectBikeIndex() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                bikeIndexRepository.disconnectFromBikeIndex()
                _profile.value = null
                _connectionState.value = BikeIndexConnectionState.NOT_CONNECTED
            } catch (e: Exception) {
                _error.value = e.message ?: "Disconnection failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setConnectionState(state: BikeIndexConnectionState) {
        _connectionState.value = state
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun loadConnectionState() {
        // Load from stored preferences/database
        viewModelScope.launch {
            try {
                // TODO: Load from storage
                _connectionState.value = BikeIndexConnectionState.NOT_CONNECTED
            } catch (e: Exception) {
                _connectionState.value = BikeIndexConnectionState.UNKNOWN
            }
        }
    }
}
