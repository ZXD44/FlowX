package com.botgta.autobot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botgta.autobot.data.TranslationManager
import com.botgta.autobot.ui.components.JobCard

@Composable
fun ActivitySelectionScreen(
    selectedJob: String, 
    onJobSelect: (String) -> Unit, 
    limeGreen: Color,
    isDarkMode: Boolean
) {
    val t = TranslationManager
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val secondaryInk = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF555555)

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = t.getString("choose_quest"), color = inkColor, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        Text(text = t.getString("quest_desc"), color = secondaryInk, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        JobCard(
            title = t.getString("fishing_title"), 
            desc = t.getString("fishing_desc"), 
            icon = "🐟", 
            isSelected = selectedJob == "ตกปลา", 
            onClick = { onJobSelect("ตกปลา") }, 
            limeGreen = limeGreen, 
            isDarkMode = isDarkMode
        )
        Spacer(modifier = Modifier.height(16.dp))
        JobCard(
            title = t.getString("mining_title"), 
            desc = t.getString("mining_desc"), 
            icon = "⛏️", 
            isSelected = selectedJob == "ขุดเหมือง", 
            onClick = { onJobSelect("ขุดเหมือง") }, 
            limeGreen = limeGreen, 
            isDarkMode = isDarkMode
        )
    }
}
