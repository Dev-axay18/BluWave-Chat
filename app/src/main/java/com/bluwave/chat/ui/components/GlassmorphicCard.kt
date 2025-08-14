package com.bluwave.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bluwave.chat.ui.theme.GlassSemiTransparent
import com.bluwave.chat.ui.theme.Indigo
import com.bluwave.chat.ui.theme.NeonPurple

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GlassSemiTransparent.copy(alpha = 0.1f),
                        GlassSemiTransparent.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        // Background blur effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.1f),
                            Indigo.copy(alpha = 0.05f)
                        )
                    )
                )
        )
        
        // Content
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun GlassmorphicCardWithBorder(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonPurple,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSemiTransparent.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = 0.8f),
                    borderColor.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GlassSemiTransparent.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
