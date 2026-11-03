package com.botgta.autobot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botgta.autobot.data.TranslationManager

/**
 * Shared UI components used across multiple screens.
 */

@Composable
fun JobCard(
    title: String,
    desc: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    limeGreen: Color,
    isDarkMode: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "job_card_scale")
    
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val secondaryInk = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF555555)
    val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = if (isSelected) limeGreen else paperColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, inkColor),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .neumorphic(radius = 12.dp, elevation = 6.dp, isDarkMode = isDarkMode)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(paperColor, RoundedCornerShape(8.dp))
                    .border(2.dp, inkColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = inkColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Text(text = desc, color = if (isSelected) inkColor else secondaryInk, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, null, tint = inkColor)
            }
        }
    }
}

@Composable
fun TransactionRow(
    icon: ImageVector,
    title: String,
    status: String,
    isOk: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val secondaryInk = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF555555)
    val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val statusColor = if (isOk) Color(0xFF10B981) else Color(0xFFEF4444)
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "row_bounce")
    
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = paperColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, inkColor),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .neumorphic(radius = 12.dp, elevation = 4.dp, isDarkMode = isDarkMode)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusColor, RoundedCornerShape(8.dp))
                    .border(2.dp, inkColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = inkColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = inkColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape).border(1.dp, inkColor, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(text = status, color = secondaryInk, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = inkColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AnimatedNotification(
    message: String?,
    type: String,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        if (message != null) {
            val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
            val highlightColor = if (isDarkMode) Color(0xFFA855F7) else Color(0xFFFDE047)
            val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
            
            val boxColor = when(type) {
                "success" -> highlightColor
                "error" -> Color(0xFFEF4444)
                else -> paperColor
            }

            Surface(
                color = boxColor,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, inkColor),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .neumorphic(radius = 8.dp, elevation = 10.dp, isDarkMode = isDarkMode)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.botgta.autobot.utils.TextUtils.parseMarkdown(message),
                        color = inkColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )                }
            }
        }
    }
}
