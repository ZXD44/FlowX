package com.botgta.autobot.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botgta.autobot.data.SecureStorage
import com.botgta.autobot.ui.components.neumorphic
import com.botgta.autobot.ui.components.AnimatedNotification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class SlipVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecureStorage.init(this)
        
        setContent {
            val isDarkMode = SecureStorage.isDarkMode()
            val scope = rememberCoroutineScope()
            
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            var isVerifying by remember { mutableStateOf(false) }
            var notificationMessage by remember { mutableStateOf<String?>(null) }
            var notificationType by remember { mutableStateOf("success") }

            val bgColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
            val paperColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
            val inkColor = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1E293B)
            val highlightColor = if (isDarkMode) Color(0xFFA855F7) else Color(0xFFFDE047)

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    bitmap = try {
                        if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(contentResolver, it)
                        } else {
                            val source = ImageDecoder.createSource(contentResolver, it)
                            ImageDecoder.decodeBitmap(source)
                        }
                    } catch (e: Exception) { null }
                }
            }

            fun startVerify() {
                if (bitmap == null) return
                isVerifying = true
                scope.launch {
                    try {
                        val image = InputImage.fromBitmap(bitmap!!, 0)
                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        val result = recognizer.process(image).await()
                        val rawText = result.text.replace("\n", " ").replace(Regex("\\s+"), " ")
                        
                        val hasMyName = rawText.contains("Sahaphum", ignoreCase = true) || 
                                        rawText.contains("ZirconX", ignoreCase = true) ||
                                        rawText.contains("สหภูมิ", ignoreCase = true) ||
                                        rawText.contains("ทับทวี", ignoreCase = true) ||
                                        rawText.contains("7098")
                        
                        // Flexible amount check: handles "20.00", "20 . 00", "20,00"
                        val amountRegex = Regex("""20[.,\s]{1,3}00""")
                        val hasCorrectAmount = amountRegex.containsMatchIn(rawText)
                        
                        // Find Transaction ID (Usually 10-15 digits for TrueMoney)
                        val txIdRegex = Regex("""500\d{8,15}""")
                        val txId = txIdRegex.find(rawText)?.value ?: run {
                            // Fallback to any 10-18 digit number if 500 prefix not found
                            Regex("""\d{10,18}""").find(rawText)?.value ?: "UNKNOWN"
                        }
                        val isUsed = SecureStorage.isSlipUsed(txId)
                        
                        if (rawText.length < 5) {
                            notificationMessage = "ไม่พบข้อมูลในรูปภาพ กรุณาใช้สลิปที่ชัดเจน"
                            notificationType = "error"
                        } else if (isUsed && txId != "UNKNOWN") {
                            notificationMessage = "สลิปนี้ถูกใช้งานไปแล้ว"
                            notificationType = "error"
                        } else if (hasMyName && hasCorrectAmount) {
                            SecureStorage.saveUsedSlip(txId)
                            val current = SecureStorage.getTokenRemainingSeconds()
                            SecureStorage.setTokenRemainingSeconds(current + 43200L)
                            notificationMessage = "ยืนยันสลิปสำเร็จ! เพิ่มเวลา 12 ชม. แล้ว"
                            notificationType = "success"
                        } else {
                            val reason = when {
                                !hasMyName -> "ชื่อผู้รับโอนไม่ถูกต้อง"
                                !hasCorrectAmount -> "ยอดเงินไม่ใช่ 20.00 บาท"
                                else -> "ข้อมูลสลิปไม่ถูกต้อง"
                            }
                            notificationMessage = reason
                            notificationType = "error"
                        }
                    } catch (e: Exception) {
                        notificationMessage = "ข้อผิดพลาด: ${e.message}"
                        notificationType = "error"
                    }
                    isVerifying = false
                    launch { delay(4000); notificationMessage = null }
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Header
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = inkColor) }
                        Column {
                            Text("ยืนยันสลิปโอนเงิน", color = inkColor, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text("แพ็กเกจ: 20 บาท / 12 ชั่วโมง", color = inkColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(Modifier.height(30.dp))

                    // Image Canvas (Doodle Style)
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(360.dp).neumorphic(radius = 16.dp, elevation = 8.dp, isDarkMode = isDarkMode),
                        shape = RoundedCornerShape(16.dp), color = paperColor, border = androidx.compose.foundation.BorderStroke(3.dp, inkColor)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                            if (bitmap != null) {
                                Image(bitmap!!.asImageBitmap(), null, modifier = Modifier.fillMaxSize())
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Description, null, modifier = Modifier.size(72.dp), tint = inkColor.copy(alpha = 0.15f))
                                    Spacer(Modifier.height(12.dp))
                                    Text("กรุณาเลือกรูปภาพสลิป", color = inkColor.copy(alpha = 0.4f), fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Buttons
                    Button(
                        onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth().height(58.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = paperColor), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(3.dp, inkColor)
                    ) {
                        Icon(Icons.Default.FileUpload, null, tint = inkColor)
                        Spacer(Modifier.width(10.dp))
                        Text("เลือกสลิปจากแกลเลอรี", color = inkColor, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { startVerify() }, modifier = Modifier.fillMaxWidth().height(58.dp),
                        enabled = bitmap != null && !isVerifying, colors = ButtonDefaults.buttonColors(containerColor = highlightColor), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(3.dp, inkColor)
                    ) {
                        if (isVerifying) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = inkColor, strokeWidth = 3.dp)
                        else {
                            Icon(Icons.Default.AutoFixHigh, null, tint = inkColor)
                            Spacer(Modifier.width(10.dp))
                            Text("ตรวจสอบข้อมูลสลิป", color = inkColor, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Notification Overlay
                Box(modifier = Modifier.fillMaxSize().padding(top = 15.dp), contentAlignment = Alignment.TopCenter) {
                    AnimatedNotification(message = notificationMessage, type = notificationType, isDarkMode = isDarkMode)
                }
            }
        }
    }
}
