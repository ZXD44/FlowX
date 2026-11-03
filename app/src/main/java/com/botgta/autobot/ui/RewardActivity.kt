package com.botgta.autobot.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.botgta.autobot.data.SecureStorage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class RewardActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val targetUrl = intent.getStringExtra("TARGET_URL") ?: "https://www.zirconx.my"

        setContent {
            var progress by remember { mutableFloatStateOf(0f) }
            var timeLeft by remember { mutableIntStateOf(10) }
            var isComplete by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                for (i in 1..100) {
                    delay(100)
                    progress = i / 100f
                    if (i % 10 == 0) timeLeft = 10 - (i / 10)
                }
                isComplete = true
                grantReward(this@RewardActivity)
            }

            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1B1D21))) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with Countdown
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(Color(0xFFFDE047)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isComplete) "✅ สำเร็จ! กำลังบันทึกเวลา..." else "🛡️ กรุณารอ: $timeLeft วินาที",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.width(200.dp).height(6.dp),
                                color = Color(0xFFA855F7),
                                trackColor = Color.Black.copy(alpha = 0.2f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                    
                    // WebView
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36"
                                
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        return false // Stay in WebView
                                    }
                                }
                                loadUrl(targetUrl)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    private fun grantReward(context: android.content.Context) {
        SecureStorage.init(context)
        val currentSecs = SecureStorage.getTokenRemainingSeconds()
        val maxStockSecs = 32400L // 9 hours
        val newSecs = (currentSecs + 3600L).coerceAtMost(maxStockSecs) // +1 hour for website
        SecureStorage.setTokenRemainingSeconds(newSecs)
        SecureStorage.incrementWebsiteVisitsToday()
        SecureStorage.setLastRewardTimestamp(System.currentTimeMillis())
        
        // เราจะไม่ใช้ finish() ทันทีเพื่อให้ User เห็นว่ามันสำเร็จ
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            finish()
        }, 1500)
    }
}

