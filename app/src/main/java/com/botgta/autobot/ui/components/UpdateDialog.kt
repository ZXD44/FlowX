package com.botgta.autobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.botgta.autobot.data.TranslationManager

@Composable
fun BeautifulUpdateDialog(
    version: String,
    changelog: String,
    downloadProgress: Float,
    isDarkMode: Boolean,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    isForceUpdate: Boolean = false
) {
    val t = TranslationManager
    val inkColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    val highlightColor = if (isDarkMode) Color(0xFFA855F7) else Color(0xFFFDE047)
    val paperColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)

    Dialog(
        onDismissRequest = { if (!isForceUpdate) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isForceUpdate, dismissOnClickOutside = !isForceUpdate)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .neumorphic(radius = 16.dp, elevation = 12.dp, isDarkMode = isDarkMode),
            shape = RoundedCornerShape(16.dp),
            color = paperColor,
            border = androidx.compose.foundation.BorderStroke(3.dp, inkColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨",
                    fontSize = 40.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = t.getString("update_title"),
                    color = inkColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = highlightColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, inkColor)
                ) {
                    Text(
                        text = "V$version",
                        color = inkColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(Modifier.height(20.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(inkColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(1.dp, inkColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = t.getString("update_changelog"),
                        color = inkColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = com.botgta.autobot.utils.TextUtils.parseMarkdown(changelog),
                        color = inkColor.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (downloadProgress > 0f) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth().height(12.dp).border(2.dp, inkColor, RoundedCornerShape(6.dp)),
                            color = highlightColor,
                            trackColor = inkColor.copy(alpha = 0.1f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = t.getString("update_downloading") + " (${(downloadProgress * 100).toInt()}%)",
                            color = inkColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isForceUpdate) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)
                            ) {
                                Text(t.getString("btn_cancel"), color = inkColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = onUpdate,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = highlightColor),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, inkColor)
                        ) {
                            Text(t.getString("btn_update_now"), color = inkColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
