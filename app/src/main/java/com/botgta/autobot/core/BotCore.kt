package com.botgta.autobot.core

import android.content.Context
import android.util.Log
import com.botgta.autobot.services.GestureControlService
import com.botgta.autobot.logic.FishingManager
import kotlinx.coroutines.*
import kotlin.random.Random

data class BotResult(
    val label: String,
    val isStopSignal: Boolean = false
)

/**
 * BotCore — Engine
 * หัวใจหลักที่ควบคุมลูปการทำงานของบอททั้งหมด
 */
object BotCore {

    private const val TAG = "BotController"

    // Coroutine scope สำหรับ bot loop (cancel ได้ทันที)
    private var botJob: Job? = null
    private val botScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // สถานะบอท
    var isRunning = false
        private set
        
    var isServiceActive = false // สำหรับเช็คใน UI เพื่อเปลี่ยนปุ่ม Play/Stop
    var lastDetectedChar = "-"
    var targetFoundCount = 0
    var tokenTimeRemainingSeconds = 10800L // 3 hours in seconds (shared status)

    // === Settings UI State ===
    var botSpeedMultiplier = 1.0f  // 1.0 = Normal, 0.5 = Fast, 1.5 = Safe
    var useHumanization = true
    var useAdvancedBreak = false
    var isMiningMode = false
    var showScanArea = true
    var latestGeneratedFreeKey: String? = null

    // === Config (ประหยัดแบตเตอรี่ & ลดความร้อน) ===
    private val SCAN_DELAY_MIN = 100L
    private val SCAN_DELAY_MAX = 150L
    private val PRE_TAP_DELAY_MIN = 20L
    private val PRE_TAP_DELAY_MAX = 50L
    private val COOLDOWN_MIN = 80L
    private val COOLDOWN_MAX = 150L
    private val PIXEL_OFFSET = 12

    private var runStartTime = 0L

    private var lastSecurityCheckTime = 0L

    fun start(context: Context) {
        if (isRunning) return
        
        // 🛡️ [CRITICAL SECURITY] ตรวจสอบลายเซ็นแอปเพียงครั้งเดียวตอนเริ่ม (Heavy operation)
        if (!com.botgta.autobot.data.SecurityUtils.isSignatureValid(context)) {
            Log.e(TAG, "Error 0x01: App integrity compromised.")
            return
        }

        Log.d(TAG, "Status: ON")
        isRunning = true
        runStartTime = System.currentTimeMillis()
        lastSecurityCheckTime = 0L // Force check on first loop
        botJob = botScope.launch { runBotLoop(context.applicationContext) }
    }

    fun stop() {
        Log.d(TAG, "Status: OFF")
        isRunning = false
        botJob?.cancel()
        botJob = null
    }

    private suspend fun runBotLoop(context: Context) {
        Log.d(TAG, "Bot loop ENTERED - Mining Mode: $isMiningMode")

        while (isRunning) {
            try {
                val currentTime = System.currentTimeMillis()
                
                // 🛡️ [SECURITY & LICENSE CHECK] ตรวจสอบทุกๆ 3 วินาทีเพื่อให้หยุดทันทีเมื่อเวลาหมด
                if (currentTime - lastSecurityCheckTime > 3000L) {
                    val isDeviceAuthorized = com.botgta.autobot.data.SecurityUtils.isDeviceAuthorized(context)
                    val timeRemaining = com.botgta.autobot.data.SecureStorage.getTokenRemainingSeconds()
                    
                    if (timeRemaining <= 0 && !isDeviceAuthorized) {
                        Log.e(TAG, "Error 0x02: Resource limits exceeded. Terminating.")
                        isRunning = false // Force stop immediately
                        stop()
                        break
                    }
                    lastSecurityCheckTime = currentTime
                }

                // Advanced Anti-ban: Break time check
                if (useAdvancedBreak) {
                    val runtimeSecs = (System.currentTimeMillis() - runStartTime) / 1000
                    if (runtimeSecs > 0 && runtimeSecs % 3600 == 0L) { // พักทุก 1 ชม.
                        Log.w(TAG, "☕ Anti-Ban: หยุดพักเบรก 2 นาที...")
                        delay(2 * 60 * 1000L) 
                    }
                }

                val clickService = GestureControlService.instance
                if (clickService == null) { delay(500); continue }
                
                val frame = clickService.captureScreen()
                if (frame == null || frame.isRecycled) {
                    delay((randomDelay(SCAN_DELAY_MIN, SCAN_DELAY_MAX) * botSpeedMultiplier).toLong())
                    continue
                }

                // วิเคราะห์ภาพและมอบหมายให้ Manager ของโหมดนั้นๆ จัดการ (หา, หน่วงเวลา, ตรวจซ้ำ, และกด)
                val result = if (isMiningMode) {
                    com.botgta.autobot.logic.MiningManager.processFrameAndClick(frame, botSpeedMultiplier, useHumanization)
                } else {
                    com.botgta.autobot.logic.FishingManager.processFrameAndClick(frame, botSpeedMultiplier, useHumanization)
                }

                if (result != null) {
                    if (result.isStopSignal) {
                        Log.w(TAG, "🛑 กระเป๋าเต็ม -> สั่งหยุดบอท")
                        stop()
                        break
                    }

                    targetFoundCount++
                    lastDetectedChar = result.label
                } else {
                    delay((randomDelay(SCAN_DELAY_MIN, SCAN_DELAY_MAX) * botSpeedMultiplier).toLong())
                }

            } catch (e: CancellationException) {
                break
            } catch (e: Exception) {
                delay(500)
            }
        }
        isRunning = false
    }

    private fun randomDelay(min: Long, max: Long): Long {
        return Random.nextLong(min, max + 1)
    }
}
