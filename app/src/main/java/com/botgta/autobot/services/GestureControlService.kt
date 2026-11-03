package com.botgta.autobot.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.random.Random

/**
 * GestureControlService — Engine
 * ระบบนิ้วมือของบอท: จำลองการแตะหน้าจอผ่าน AccessibilityService
 */
class GestureControlService : AccessibilityService() {

    companion object {
        private const val TAG = "GestureControlService"

        // Singleton reference
        var instance: GestureControlService? = null
            private set
    }

    // Lock ป้องกันการคลิกซ้อนกัน
    private val isPerformingGesture = AtomicBoolean(false)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "✅ AutoClickService CONNECTED — พร้อมใช้งาน")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // ไม่ต้องทำอะไร — เราใช้แค่ dispatchGesture
    }

    override fun onInterrupt() {
        Log.w(TAG, "onInterrupt called")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Log.d(TAG, "AutoClickService UNBOUND")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        Log.d(TAG, "AutoClickService DESTROYED")
        super.onDestroy()
    }

    /**
     * สั่งแตะหน้าจอที่พิกัด (x, y) — รองรับ Coroutine
     * - Tap Duration สุ่มระหว่าง 40-90ms (เนียนเหมือนนิ้วคน)
     * - ป้องกัน gesture ซ้อนกัน (lock mechanism)
     */
    suspend fun performTap(x: Float, y: Float): Boolean {
        // ตรวจสอบ lock
        if (!isPerformingGesture.compareAndSet(false, true)) {
            Log.w(TAG, "⚠️ Gesture lock active — ข้ามการ tap ที่ ($x, $y)")
            return false
        }

        return try {
            val result = executeTap(x, y)
            Log.d(TAG, "Tap at ($x, $y) -> ${if (result) "SUCCESS" else "FAILED"}")
            result
        } finally {
            isPerformingGesture.set(false)
        }
    }

    /**
     * ทำ Gesture Tap จริงๆ ผ่าน AccessibilityService API
     */
    private suspend fun executeTap(x: Float, y: Float): Boolean {
        return suspendCancellableCoroutine { continuation ->
            // สุ่ม tap duration ให้มีความเสถียร (40-70ms) เพื่อให้เกมรับรู้การกดได้แน่นอนขึ้น
            val tapDuration = Random.nextLong(40, 71)
            Log.d(TAG, "Tap duration: ${tapDuration}ms at ($x, $y)")

            val path = Path().apply {
                moveTo(x, y)
            }

            val stroke = GestureDescription.StrokeDescription(
                path,
                0,            // startTime
                tapDuration   // duration สุ่ม
            )

            val gesture = GestureDescription.Builder()
                .addStroke(stroke)
                .build()

            val dispatched = dispatchGesture(
                gesture,
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        Log.d(TAG, "✅ Gesture completed at ($x, $y)")
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        Log.w(TAG, "❌ Gesture cancelled at ($x, $y)")
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                },
                null  // handler — ใช้ main thread
            )

            if (!dispatched) {
                Log.e(TAG, "❌ dispatchGesture returned false!")
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
    }

    /**
     * ดึงภาพหน้าจอโดยไม่ต้องใช้ Screen Record Permission (Android 11+)
     * ภาพที่ได้เป็น Hardware Bitmap
     */
    suspend fun captureScreen(): android.graphics.Bitmap? = suspendCancellableCoroutine { continuation ->
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                takeScreenshot(
                    android.view.Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshot: ScreenshotResult) {
                            try {
                                val hwBuffer = screenshot.hardwareBuffer
                                val colorSpace = screenshot.colorSpace
                                val hwBitmap = android.graphics.Bitmap.wrapHardwareBuffer(hwBuffer, colorSpace)
                                
                                // แปลงเป็น Software Bitmap เพื่อให้นำไปทำ ROI Crop รูปต่อได้
                                val swBitmap = hwBitmap?.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                                hwBitmap?.recycle()
                                hwBuffer.close()
                                
                                if (continuation.isActive) continuation.resume(swBitmap)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing screenshot: ${e.message}")
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }

                        override fun onFailure(errorCode: Int) {
                            Log.e(TAG, "takeScreenshot failed with errorCode: $errorCode")
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                )
            } else {
                Log.e(TAG, "takeScreenshot is only supported on Android 11+ (API 30+)")
                if (continuation.isActive) continuation.resume(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical capture error: ${e.message}")
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
