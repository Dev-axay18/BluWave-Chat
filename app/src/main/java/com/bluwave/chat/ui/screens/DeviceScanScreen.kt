package com.bluwave.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluwave.chat.data.model.BluetoothDevice
import com.bluwave.chat.ui.components.AnimatedButton
import com.bluwave.chat.ui.components.GlassmorphicCard
import com.bluwave.chat.ui.theme.*

@Composable
fun DeviceScanScreen(
    devices: List<BluetoothDevice>,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    
    // Background gradient animation
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(DeepNavy, Indigo, DeepNavy),
                    start = androidx.compose.ui.geometry.Offset(backgroundOffset * 1000f, 0f),
                    end = androidx.compose.ui.geometry.Offset((backgroundOffset + 1f) * 1000f, 1000f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSemiTransparent.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Available Devices",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = "Select a device to join",
                        fontSize = 14.sp,
                        color = LightGray
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Refresh button
                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSemiTransparent.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = ElectricBlue
                    )
                }
            }
            
            // Device List
            if (devices.isEmpty()) {
                EmptyState(
                    isLoading = isLoading,
                    onRefreshClick = onRefreshClick
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = devices,
                        key = { it.address }
                    ) { device ->
                        DeviceItem(
                            device = device,
                            onClick = { onDeviceClick(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: BluetoothDevice,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 0.3f,
        animationSpec = tween(durationMillis = 150),
        label = "glow"
    )

    GlassmorphicCardWithBorder(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(150)
                        isPressed = false
                    }
                }
            ),
        borderColor = if (device.isConnected) ConnectedGreen else ElectricBlue
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon with status
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (device.isConnected) ConnectedGreen else ElectricBlue,
                                if (device.isConnected) ConnectedGreen.copy(alpha = 0.3f) else ElectricBlue.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (device.isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                    contentDescription = "Device",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Device info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
                Text(
                    text = device.address,
                    fontSize = 12.sp,
                    color = LightGray
                )
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (device.isConnected) ConnectedGreen else DisconnectedRed
                    )
            )
        }
        
        // Glow effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            (if (device.isConnected) ConnectedGreen else ElectricBlue).copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun EmptyState(
    isLoading: Boolean,
    onRefreshClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = ElectricBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Scanning for devices...",
                fontSize = 16.sp,
                color = LightGray
            )
        } else {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = "No devices",
                modifier = Modifier.size(64.dp),
                tint = LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No devices found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Make sure Bluetooth is enabled and devices are discoverable",
                fontSize = 14.sp,
                color = LightGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedButton(
                text = "Refresh",
                onClick = onRefreshClick,
                modifier = Modifier.height(48.dp)
            )
        }
    }
}
