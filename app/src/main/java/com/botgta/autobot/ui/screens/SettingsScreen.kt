package com.botgta.autobot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botgta.autobot.data.SecureStorage
import com.botgta.autobot.data.TranslationManager
import com.botgta.autobot.ui.components.neumorphic

@Composable
fun SettingsScreen(
    version: String, 
    onlineVersionName: String, 
    onCheckUpdate: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isAuthorized: Boolean = false
) {
    val t = TranslationManager
    val highlightColor = if (isDarkMode) Color(0xFFA855F7) else Color(0xFFFDE047)
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val secondaryInk = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF555555)
    val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    
    var botSpeed by remember { mutableStateOf(com.botgta.autobot.core.BotCore.botSpeedMultiplier) }
    var humanize by remember { mutableStateOf(com.botgta.autobot.core.BotCore.useHumanization) }
    var advancedBreak by remember { mutableStateOf(com.botgta.autobot.core.BotCore.useAdvancedBreak) }
    var showScanArea by remember { mutableStateOf(com.botgta.autobot.core.BotCore.showScanArea) }
    var currentLanguage by remember { mutableStateOf(SecureStorage.getLanguage()) }
    var isAdminUnlimited by remember { mutableStateOf(SecureStorage.isAdminUnlimitedEnabled()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = t.getString("system_config"), color = inkColor, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
            Text(text = t.getString("config_desc"), color = secondaryInk, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        item {
            Surface(
                color = paperColor,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, inkColor),
                modifier = Modifier.fillMaxWidth().neumorphic(radius = 12.dp, elevation = 6.dp, isDarkMode = isDarkMode)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(t.getString("display_themes"), color = inkColor, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(16.dp))
                    
                    // Language Switcher
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = inkColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(t.getString("language_setting"), color = inkColor, modifier = Modifier.weight(1f), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        
                        // TH / EN Toggle
                        Row(
                            modifier = Modifier
                                .background(secondaryInk.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, inkColor, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            listOf("TH", "EN").forEach { lang ->
                                val isSelected = currentLanguage == lang
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) highlightColor else Color.Transparent)
                                        .clickable {
                                            currentLanguage = lang
                                            SecureStorage.setLanguage(lang)
                                            TranslationManager.currentLang = if (lang == "EN") TranslationManager.Lang.EN else TranslationManager.Lang.TH
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = lang, color = inkColor, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null, tint = inkColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(t.getString("lantern_mode"), color = inkColor, modifier = Modifier.weight(1f), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = inkColor, checkedTrackColor = highlightColor, uncheckedThumbColor = secondaryInk, uncheckedTrackColor = paperColor)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, null, tint = inkColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(t.getString("show_scanner"), color = inkColor, modifier = Modifier.weight(1f), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = showScanArea,
                            onCheckedChange = { 
                                showScanArea = it
                                com.botgta.autobot.core.BotCore.showScanArea = it
                                com.botgta.autobot.data.SecureStorage.setShowScanArea(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = inkColor, checkedTrackColor = highlightColor, uncheckedThumbColor = secondaryInk, uncheckedTrackColor = paperColor)
                        )
                    }
                }
            }
        }

        if (isAuthorized) {
            item {
                Surface(
                    color = paperColor,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, inkColor),
                    modifier = Modifier.fillMaxWidth().neumorphic(radius = 12.dp, elevation = 6.dp, isDarkMode = isDarkMode)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(t.getString("behavior_speed"), color = inkColor, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.height(16.dp))
                        
                        Text(t.getString("action_speed") + String.format("%.1f", botSpeed), color = inkColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Slider(
                            value = botSpeed,
                            onValueChange = { 
                                botSpeed = it
                                com.botgta.autobot.core.BotCore.botSpeedMultiplier = it
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = inkColor, activeTrackColor = highlightColor)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.getString("humanize_actions"), color = inkColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(t.getString("humanize_desc"), color = secondaryInk, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Switch(
                                checked = humanize,
                                onCheckedChange = { 
                                    humanize = it
                                    com.botgta.autobot.core.BotCore.useHumanization = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = inkColor, checkedTrackColor = highlightColor, uncheckedThumbColor = secondaryInk, uncheckedTrackColor = paperColor)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.getString("anti_afk_rune"), color = inkColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(t.getString("anti_afk_desc"), color = secondaryInk, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Switch(
                                checked = advancedBreak,
                                onCheckedChange = { 
                                    advancedBreak = it
                                    com.botgta.autobot.core.BotCore.useAdvancedBreak = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = inkColor, checkedTrackColor = highlightColor, uncheckedThumbColor = secondaryInk, uncheckedTrackColor = paperColor)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = inkColor.copy(alpha = 0.1f))
                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.getString("admin_unlimited"), color = inkColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                                Text(t.getString("admin_unlimited_desc"), color = secondaryInk, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Switch(
                                checked = isAdminUnlimited,
                                onCheckedChange = { 
                                    isAdminUnlimited = it
                                    SecureStorage.setAdminUnlimited(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = inkColor, checkedTrackColor = highlightColor, uncheckedThumbColor = secondaryInk, uncheckedTrackColor = paperColor)
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Surface(
                color = paperColor,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, inkColor),
                modifier = Modifier.fillMaxWidth().neumorphic(radius = 12.dp, elevation = 6.dp, isDarkMode = isDarkMode)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t.getString("app_info"), color = inkColor, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(16.dp))
                    
                    Text(t.getString("curr_version") + (if (onlineVersionName != "...") onlineVersionName else version), color = inkColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    
                    Spacer(Modifier.height(4.dp))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val deviceID = com.botgta.autobot.data.SecurityUtils.getDeviceID(context)
                    Text("Device ID: $deviceID", color = secondaryInk, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Created by ZirconX", color = highlightColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = onCheckUpdate,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = inkColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t.getString("btn_check_update"), color = inkColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
