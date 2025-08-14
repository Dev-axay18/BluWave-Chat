package com.bluwave.chat.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val isEncrypted: Boolean = true,
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    SYSTEM,
    CONNECTION_STATUS
}

data class BluetoothDevice(
    val address: String,
    val name: String,
    val isConnected: Boolean = false,
    val isHost: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

data class ChatSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val isHost: Boolean = false,
    val hostDevice: BluetoothDevice? = null,
    val connectedDevices: List<BluetoothDevice> = emptyList(),
    val groupKey: ByteArray? = null,
    val isActive: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatSession

        if (sessionId != other.sessionId) return false
        if (isHost != other.isHost) return false
        if (hostDevice != other.hostDevice) return false
        if (connectedDevices != other.connectedDevices) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + isHost.hashCode()
        result = 31 * result + (hostDevice?.hashCode() ?: 0)
        result = 31 * result + connectedDevices.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }
}
