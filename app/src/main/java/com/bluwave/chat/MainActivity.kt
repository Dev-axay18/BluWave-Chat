package com.bluwave.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluwave.chat.ui.screens.*
import com.bluwave.chat.ui.theme.BluWaveChatTheme
import com.bluwave.chat.ui.viewmodel.BluetoothViewModel
import com.bluwave.chat.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BluWaveChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BluWaveChatApp()
                }
            }
        }
    }
}

@Composable
fun BluWaveChatApp(
    viewModel: BluetoothViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Error handling
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // In a real app, you'd show a snackbar or toast here
            // For now, we'll just clear the error after a delay
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    when (currentScreen) {
        Screen.WELCOME -> {
            WelcomeScreen(
                onHostChat = {
                    viewModel.startHostMode()
                },
                onJoinChat = {
                    viewModel.scanForDevices()
                }
            )
        }
        
        Screen.DEVICE_SCAN -> {
            DeviceScanScreen(
                devices = uiState.availableDevices,
                onDeviceClick = { device ->
                    viewModel.joinHostMode(device)
                },
                onBackClick = {
                    viewModel.navigateToScreen(Screen.WELCOME)
                },
                onRefreshClick = {
                    viewModel.refreshDeviceScan()
                },
                isLoading = isLoading
            )
        }
        
        Screen.CHAT -> {
            ChatScreen(
                messages = uiState.messages,
                connectedDevices = uiState.connectedDevices,
                isHost = uiState.isHost,
                onSendMessage = { message ->
                    viewModel.sendMessage(message)
                },
                onDisconnect = {
                    viewModel.disconnect()
                }
            )
        }
        
        Screen.SETTINGS -> {
            // Settings screen would go here
            // For now, we'll just show a placeholder
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Settings content would go here
            }
        }
    }
}
