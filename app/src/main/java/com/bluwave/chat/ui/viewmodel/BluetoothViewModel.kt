package com.bluwave.chat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bluwave.chat.bluetooth.BluetoothManager
import com.bluwave.chat.crypto.CryptoManager
import com.bluwave.chat.data.model.BluetoothDevice
import com.bluwave.chat.data.model.ChatMessage
import com.bluwave.chat.data.model.ChatSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    
    private val cryptoManager = CryptoManager()
    private val bluetoothManager = BluetoothManager(application, cryptoManager)
    
    // UI State
    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()
    
    private val _currentScreen = MutableStateFlow(Screen.WELCOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe Bluetooth state changes
            bluetoothManager.isHost.collect { isHost ->
                _uiState.value = _uiState.value.copy(isHost = isHost)
            }
        }
        
        viewModelScope.launch {
            bluetoothManager.isConnected.collect { isConnected ->
                _uiState.value = _uiState.value.copy(isConnected = isConnected)
                if (isConnected && _currentScreen.value == Screen.DEVICE_SCAN) {
                    _currentScreen.value = Screen.CHAT
                }
            }
        }
        
        viewModelScope.launch {
            bluetoothManager.connectedDevices.collect { devices ->
                _uiState.value = _uiState.value.copy(connectedDevices = devices)
            }
        }
        
        viewModelScope.launch {
            bluetoothManager.scanResults.collect { devices ->
                _uiState.value = _uiState.value.copy(availableDevices = devices)
            }
        }
        
        viewModelScope.launch {
            bluetoothManager.messages.collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun startHostMode() {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            if (!bluetoothManager.isBluetoothSupported()) {
                _errorMessage.value = "Bluetooth is not supported on this device"
                _isLoading.value = false
                return
            }
            
            if (!bluetoothManager.isBluetoothEnabled()) {
                _errorMessage.value = "Please enable Bluetooth"
                _isLoading.value = false
                return
            }
            
            bluetoothManager.startHostMode()
            _currentScreen.value = Screen.CHAT
            _isLoading.value = false
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start host mode: ${e.message}"
            _isLoading.value = false
        }
    }

    fun joinHostMode(hostDevice: BluetoothDevice) {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            if (!bluetoothManager.isBluetoothSupported()) {
                _errorMessage.value = "Bluetooth is not supported on this device"
                _isLoading.value = false
                return
            }
            
            if (!bluetoothManager.isBluetoothEnabled()) {
                _errorMessage.value = "Please enable Bluetooth"
                _isLoading.value = false
                return
            }
            
            bluetoothManager.joinHostMode(hostDevice)
            _currentScreen.value = Screen.CHAT
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to connect to host: ${e.message}"
            _isLoading.value = false
        }
    }

    fun scanForDevices() {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            bluetoothManager.scanForDevices()
            _currentScreen.value = Screen.DEVICE_SCAN
            _isLoading.value = false
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to scan for devices: ${e.message}"
            _isLoading.value = false
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        try {
            bluetoothManager.sendMessage(text)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to send message: ${e.message}"
        }
    }

    fun disconnect() {
        bluetoothManager.disconnect()
        _currentScreen.value = Screen.WELCOME
        _uiState.value = BluetoothUiState()
    }

    fun navigateToScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshDeviceScan() {
        bluetoothManager.scanForDevices()
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothManager.disconnect()
    }
}

data class BluetoothUiState(
    val isHost: Boolean = false,
    val isConnected: Boolean = false,
    val connectedDevices: List<BluetoothDevice> = emptyList(),
    val availableDevices: List<BluetoothDevice> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentSession: ChatSession? = null
)

enum class Screen {
    WELCOME,
    DEVICE_SCAN,
    CHAT,
    SETTINGS
}
