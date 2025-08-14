package com.bluwave.chat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.bluwave.chat.data.model.BluetoothDevice as AppBluetoothDevice
import com.bluwave.chat.data.model.ChatMessage
import com.bluwave.chat.data.model.ChatSession
import com.bluwave.chat.crypto.CryptoManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BluetoothManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {
    companion object {
        private const val TAG = "BluetoothManager"
        private const val SERVICE_NAME = "BluWaveChat"
        private val SERVICE_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        private const val MAX_CONNECTIONS = 6
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var serverSocket: BluetoothServerSocket? = null
    private val connectedSockets = ConcurrentHashMap<String, BluetoothSocket>()
    private val deviceStreams = ConcurrentHashMap<String, Pair<InputStream, OutputStream>>()
    
    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()
    
    private val _connectedDevices = MutableStateFlow<List<AppBluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<AppBluetoothDevice>> = _connectedDevices.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _scanResults = MutableStateFlow<List<AppBluetoothDevice>>(emptyList())
    val scanResults: StateFlow<List<AppBluetoothDevice>> = _scanResults.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private var hostJob: Job? = null
    private var clientJob: Job? = null
    private var messageListenerJob: Job? = null

    fun startHostMode() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported")
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth not enabled")
            return
        }
        
        _isHost.value = true
        startServer()
    }

    fun joinHostMode(hostDevice: AppBluetoothDevice) {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported")
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth not enabled")
            return
        }
        
        _isHost.value = false
        connectToHost(hostDevice)
    }

    private fun startServer() {
        hostJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                Log.d(TAG, "Server started, waiting for connections...")
                
                while (isActive && connectedSockets.size < MAX_CONNECTIONS) {
                    try {
                        val socket = serverSocket?.accept()
                        socket?.let { handleNewConnection(it) }
                    } catch (e: IOException) {
                        if (isActive) {
                            Log.e(TAG, "Error accepting connection", e)
                        }
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error starting server", e)
            } finally {
                serverSocket?.close()
            }
        }
    }

    private fun handleNewConnection(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val device = socket.remoteDevice
                val deviceId = device.address
                
                if (connectedSockets.size >= MAX_CONNECTIONS) {
                    socket.close()
                    return@launch
                }
                
                connectedSockets[deviceId] = socket
                val inputStream = socket.inputStream
                val outputStream = socket.outputStream
                deviceStreams[deviceId] = Pair(inputStream, outputStream)
                
                val appDevice = AppBluetoothDevice(
                    address = deviceId,
                    name = device.name ?: "Unknown Device",
                    isConnected = true,
                    isHost = false
                )
                
                updateConnectedDevices()
                startMessageListener(deviceId, inputStream)
                
                // Send welcome message
                val welcomeMessage = ChatMessage(
                    text = "Device ${appDevice.name} joined the chat",
                    senderId = "system",
                    senderName = "System",
                    messageType = com.bluwave.chat.data.model.MessageType.SYSTEM
                )
                broadcastMessage(welcomeMessage)
                
                Log.d(TAG, "New device connected: ${appDevice.name}")
                
            } catch (e: IOException) {
                Log.e(TAG, "Error handling new connection", e)
                socket.close()
            }
        }
    }

    private fun connectToHost(hostDevice: AppBluetoothDevice) {
        clientJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val device = bluetoothAdapter?.getRemoteDevice(hostDevice.address)
                val socket = device?.createRfcommSocketToServiceRecord(SERVICE_UUID)
                
                socket?.connect()
                
                val deviceId = hostDevice.address
                connectedSockets[deviceId] = socket!!
                val inputStream = socket.inputStream
                val outputStream = socket.outputStream
                deviceStreams[deviceId] = Pair(inputStream, outputStream)
                
                _isConnected.value = true
                updateConnectedDevices()
                startMessageListener(deviceId, inputStream)
                
                Log.d(TAG, "Connected to host: ${hostDevice.name}")
                
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to host", e)
                _isConnected.value = false
            }
        }
    }

    private fun startMessageListener(deviceId: String, inputStream: InputStream) {
        messageListenerJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (isActive) {
                try {
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        handleIncomingMessage(message, deviceId)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading from device $deviceId", e)
                    handleDeviceDisconnection(deviceId)
                    break
                }
            }
        }
    }

    private fun handleIncomingMessage(message: String, deviceId: String) {
        try {
            // For now, assume plain text. In production, this would be encrypted
            val chatMessage = ChatMessage(
                text = message,
                senderId = deviceId,
                senderName = connectedDevices.value.find { it.address == deviceId }?.name ?: "Unknown",
                isFromMe = false
            )
            
            addMessage(chatMessage)
            
            // If we're the host, forward to all other devices
            if (_isHost.value) {
                forwardMessageToOthers(chatMessage, deviceId)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming message", e)
        }
    }

    private fun forwardMessageToOthers(message: ChatMessage, excludeDeviceId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val messageText = message.text
            deviceStreams.forEach { (deviceId, streams) ->
                if (deviceId != excludeDeviceId) {
                    try {
                        streams.second.write(messageText.toByteArray())
                        streams.second.flush()
                    } catch (e: IOException) {
                        Log.e(TAG, "Error forwarding message to $deviceId", e)
                        handleDeviceDisconnection(deviceId)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val chatMessage = ChatMessage(
            text = text,
            senderId = "me",
            senderName = "Me",
            isFromMe = true
        )
        
        addMessage(chatMessage)
        
        // Send to all connected devices
        CoroutineScope(Dispatchers.IO).launch {
            deviceStreams.forEach { (deviceId, streams) ->
                try {
                    streams.second.write(text.toByteArray())
                    streams.second.flush()
                } catch (e: IOException) {
                    Log.e(TAG, "Error sending message to $deviceId", e)
                    handleDeviceDisconnection(deviceId)
                }
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun updateConnectedDevices() {
        val devices = connectedSockets.map { (address, _) ->
            AppBluetoothDevice(
                address = address,
                name = bluetoothAdapter?.getRemoteDevice(address)?.name ?: "Unknown Device",
                isConnected = true,
                isHost = false
            )
        }
        _connectedDevices.value = devices
    }

    private fun handleDeviceDisconnection(deviceId: String) {
        connectedSockets.remove(deviceId)
        deviceStreams.remove(deviceId)
        updateConnectedDevices()
        
        val disconnectMessage = ChatMessage(
            text = "Device disconnected",
            senderId = "system",
            senderName = "System",
            messageType = com.bluwave.chat.data.model.MessageType.SYSTEM
        )
        addMessage(disconnectMessage)
        
        if (connectedSockets.isEmpty()) {
            _isConnected.value = false
        }
    }

    fun scanForDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pairedDevices = bluetoothAdapter.bondedDevices
                val scanResults = pairedDevices.map { device ->
                    AppBluetoothDevice(
                        address = device.address,
                        name = device.name ?: "Unknown Device",
                        isConnected = false,
                        isHost = false
                    )
                }
                _scanResults.value = scanResults
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning for devices", e)
            }
        }
    }

    fun disconnect() {
        hostJob?.cancel()
        clientJob?.cancel()
        messageListenerJob?.cancel()
        
        connectedSockets.values.forEach { it.close() }
        connectedSockets.clear()
        deviceStreams.clear()
        
        serverSocket?.close()
        
        _isConnected.value = false
        _connectedDevices.value = emptyList()
        _messages.value = emptyList()
    }

    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
}
