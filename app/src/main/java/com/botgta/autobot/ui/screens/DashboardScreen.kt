package com.botgta.autobot.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botgta.autobot.data.SecureStorage
import com.botgta.autobot.data.TranslationManager
import com.botgta.autobot.ui.components.neumorphic

@Composable
fun BotDashboardScreen(
    version: String,
    latestVersion: String,
    isOverlayGranted: Boolean,
    isAccessibilityGranted: Boolean,
    currentTab: Int,
    selectedJob: String,
    tokenTimeSeconds: Long,
    isBotActive: Boolean,
    onTabChange: (Int) -> Unit,
    onJobSelect: (String) -> Unit,
    onEnterKey: (String) -> Unit,
    onGetFreeKey: () -> Unit,
    onShowGoogleAds: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onLaunchBot: () -> Unit,
    onCheckUpdate: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    webVisits: Int,
    isAuthorized: Boolean = false,
    isUpdateRequired: Boolean = false
) {
    // 🌍 Sync Language
    val appLang = SecureStorage.getLanguage()
    TranslationManager.currentLang = if (appLang == "EN") TranslationManager.Lang.EN else TranslationManager.Lang.TH
    val t = TranslationManager

    val bgColor = if (isDarkMode) Color(0xFF050505) else Color(0xFFF0F0F0)
    val surfaceColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val highlightColor = if (isDarkMode) Color(0xFFA855F7) else Color(0xFFFDE047)
    
    val canStart = !isUpdateRequired && (isBotActive || isAuthorized || tokenTimeSeconds > 0)

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .height(76.dp)
                    .neumorphic(radius = 12.dp, elevation = 6.dp, isDarkMode = isDarkMode),
                shape = RoundedCornerShape(12.dp),
                color = surfaceColor,
                border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        TabItem(t.getString("tab_home"), Icons.Default.Book, 0),
                        TabItem(t.getString("tab_quest"), Icons.Default.Terrain, 1),
                        TabItem(t.getString("tab_gear"), Icons.Default.Handyman, 2)
                    )
                    
                    tabs.forEach { item ->
                        val isSelected = currentTab == item.index
                        val tabInteractionSource = remember { MutableInteractionSource() }
                        val iconScale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1f, label = "icon_scale")
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = tabInteractionSource,
                                    indication = null,
                                    onClick = { onTabChange(item.index) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                                        .then(
                                            if (isSelected) Modifier
                                                .background(highlightColor, RoundedCornerShape(8.dp))
                                                .border(2.dp, inkColor, RoundedCornerShape(8.dp))
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = inkColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = item.label,
                                    color = inkColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1f, label = "fab_bounce")

            FloatingActionButton(
                onClick = onLaunchBot,
                interactionSource = interactionSource,
                containerColor = if (!canStart) Color.LightGray else if (isBotActive) Color(0xFFEF4444) else highlightColor,
                contentColor = inkColor,
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .neumorphic(radius = 30.dp, elevation = 6.dp, isDarkMode = isDarkMode)
                    .border(3.dp, inkColor, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    imageVector = if (isBotActive) Icons.Filled.Stop else Icons.Filled.PlayArrow, 
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (!canStart) Color.White else inkColor
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(paddingValues)
        ) {
            when (currentTab) {
                0 -> HomeScreen(isOverlayGranted, isAccessibilityGranted, selectedJob, tokenTimeSeconds, onEnterKey, onGetFreeKey, onShowGoogleAds, onGrantOverlay, onGrantAccessibility, highlightColor, Brush.linearGradient(listOf(highlightColor, highlightColor)), isDarkMode, webVisits, isAuthorized)
                1 -> ActivitySelectionScreen(selectedJob, onJobSelect, highlightColor, isDarkMode)
                2 -> SettingsScreen(version, latestVersion, onCheckUpdate, isDarkMode, onDarkModeChange, isAuthorized)
            }
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector, val index: Int)
