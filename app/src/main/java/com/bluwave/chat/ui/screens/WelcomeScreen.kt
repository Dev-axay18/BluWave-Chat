package com.bluwave.chat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluwave.chat.ui.components.AnimatedButton
import com.bluwave.chat.ui.theme.*

@Composable
fun WelcomeScreen(
    onHostChat: () -> Unit,
    onJoinChat: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    
    // Logo rotation animation
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_rotation"
    )
    
    // Logo scale animation
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    // Background gradient animation
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
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
        // Animated background particles
        repeat(20) { index ->
            val particleOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000 + (index * 200),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particle_$index"
            )
            
            Box(
                modifier = Modifier
                    .offset(
                        x = (particleOffset * 400f).dp,
                        y = (index * 50f).dp
                    )
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (index % 4) {
                            0 -> NeonPurple
                            1 -> HotPink
                            2 -> ElectricBlue
                            else -> NeonGreen
                        }.copy(alpha = 0.6f)
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .rotate(logoRotation)
                    .clip(RoundedCornerShape(60.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NeonPurple, HotPink, ElectricBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "BluWave Chat Logo",
                    modifier = Modifier.size(60.dp),
                    tint = White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title with shimmer effect
            val shimmerOffset by infiniteTransition.animateFloat(
                initialValue = -1f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "shimmer"
            )
            
            Text(
                text = "BluWave Chat",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                White.copy(alpha = 0.8f),
                                Color.Transparent
                            ),
                            start = androidx.compose.ui.geometry.Offset(shimmerOffset * 200f, 0f),
                            end = androidx.compose.ui.geometry.Offset((shimmerOffset + 1f) * 200f, 0f)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Secure Group Chat Over Bluetooth",
                fontSize = 18.sp,
                color = LightGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Action Buttons
            AnimatedButton(
                text = "Host Chat",
                onClick = onHostChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                isPrimary = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedButton(
                text = "Join Chat",
                onClick = onJoinChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                isPrimary = false
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Feature highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureHighlight(
                    icon = Icons.Default.Wifi,
                    text = "No Internet",
                    color = NeonGreen
                )
                FeatureHighlight(
                    icon = Icons.Default.Chat,
                    text = "6 Users Max",
                    color = ElectricBlue
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlight(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = LightGray
        )
    }
}
