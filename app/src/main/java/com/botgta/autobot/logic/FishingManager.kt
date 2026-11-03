package com.botgta.autobot.logic

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * FishingManager — Logic
 * V35: White Circle Blob Detection + OCR Priority
 */
object FishingManager {

    private const val TAG = "FishingBrain"

    var onLog: ((String) -> Unit)? = null

    private fun log(msg: String) {
        Log.d(TAG, msg)
        onLog?.invoke(msg)
    }

    private val RESTART_TEXT_TARGETS = listOf("เริ่มใหม่", "เริ่มใหม", "RESTART", "Retry", "ตกลง", "เริ่ม", "พลาด")
    private val BAG_FULL_TARGETS = listOf("กระเป๋า", "เต็มแล้ว", "ไม่สามารถเก็บ", "ปลากระเป๋า", "ALERT", "alert", "Alert")


    private const val ROI_PERCENT = 0.50f

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class DetectionResult(
        val screenX: Float,
        val screenY: Float,
        val detectedChar: String,
        val isStopSignal: Boolean = false
    )

    suspend fun processFrameAndClick(
        frame: Bitmap,
        speedMultiplier: Float,
        useHumanization: Boolean
    ): com.botgta.autobot.core.BotResult? {
        val detection = analyzeFrame(frame)
        frame.recycle() // คืน Memory ทันที
        
        if (detection == null) return null
        
        if (detection.isStopSignal) {
            if (detection.screenX > 0 && detection.screenY > 0) {
                // กดปุ่มเพื่อให้หน้าต่างหายไปก่อนจะหยุด
                val controlService = com.botgta.autobot.services.GestureControlService.instance
                if (controlService != null) {
                    controlService.performTap(detection.screenX, detection.screenY)
                    kotlinx.coroutines.delay(100)
                }
            }
            return com.botgta.autobot.core.BotResult(detection.detectedChar, true)
        }
        
        var finalX = detection.screenX
        var finalY = detection.screenY
        val label = detection.detectedChar
        
        if (useHumanization) {
            if (kotlin.random.Random.nextInt(100) < 2) {
                finalX += kotlin.random.Random.nextInt(-30, 30)
                finalY += kotlin.random.Random.nextInt(-30, 30)
                log("🤖 Humanize: จงใจคลิกพลาดนิดหน่อย")
            } else {
                finalX += kotlin.random.Random.nextInt(-12, 13)
                finalY += kotlin.random.Random.nextInt(-12, 13)
            }
        }
        
        val preTapDelay = (kotlin.random.Random.nextLong(20, 51) * speedMultiplier).toLong()
        kotlinx.coroutines.delay(preTapDelay)
        
        val controlService = com.botgta.autobot.services.GestureControlService.instance
        if (controlService != null) {
            controlService.performTap(finalX, finalY)
            kotlinx.coroutines.delay(35)
            controlService.performTap(finalX, finalY)
        }
        
        val cooldownTime = if (label.length > 2) 50L else (kotlin.random.Random.nextLong(80, 151) * speedMultiplier).toLong()
        kotlinx.coroutines.delay(cooldownTime)
        
        return com.botgta.autobot.core.BotResult(label, false)
    }

    suspend fun analyzeFrame(fullBitmap: Bitmap): DetectionResult? = withContext(Dispatchers.Default) {
        var roiBitmap: Bitmap? = null
        var scaledBitmap: Bitmap? = null
        try {
            val fullWidth = fullBitmap.width
            val fullHeight = fullBitmap.height

            val roiWidth = (fullWidth * ROI_PERCENT).toInt()
            val roiHeight = (fullHeight * ROI_PERCENT).toInt()
            val roiX = (fullWidth - roiWidth) / 2
            val roiY = ((fullHeight - roiHeight) * 0.55f).toInt()

            roiBitmap = Bitmap.createBitmap(fullBitmap, roiX, roiY, roiWidth, roiHeight)

            val targetScaleWidth = 800
            val scaleFactor = roiWidth.toFloat() / targetScaleWidth.toFloat()
            val targetScaleHeight = (roiHeight / scaleFactor).toInt()
            
            scaledBitmap = Bitmap.createScaledBitmap(roiBitmap, targetScaleWidth, targetScaleHeight, true)

            val inputImage = InputImage.fromBitmap(scaledBitmap, 0)
            val textResult = recognizeText(inputImage, scaledBitmap)

            if (textResult != null) {
                val roiScaledX = textResult.first * scaleFactor
                val roiScaledY = textResult.second * scaleFactor
                
                val globalX = roiScaledX + roiX
                val globalY = roiScaledY + roiY
                
                val isFullBag = textResult.third == "BAG_FULL"
                Log.d(TAG, "🎯 DETECTION: '${textResult.third}' at ($globalX, $globalY)")

                return@withContext DetectionResult(
                    screenX = globalX,
                    screenY = globalY,
                    detectedChar = textResult.third,
                    isStopSignal = isFullBag
                )
            }
            return@withContext null

        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            return@withContext null
        } finally {
            roiBitmap?.recycle()
            scaledBitmap?.recycle()
        }
    }

    private suspend fun recognizeText(
        inputImage: InputImage,
        originalBitmap: Bitmap
    ): Triple<Int, Int, String>? = suspendCancellableCoroutine { continuation ->
        
        // ดึงพิกเซลครั้งเดียวเพื่อใช้ในทุกลอจิก (ประหยัดหน่วยความจำและ CPU)
        val w = originalBitmap.width
        val h = originalBitmap.height
        val pixels = IntArray(w * h)
        originalBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        // --- STEP 1: ULTRA PRIORITY - WHITE BLOB (ปุ่มสีขาวกลม) ---
        val whiteBox = findWhiteButton(pixels, w, h)
        if (whiteBox != null) {
            log("⚪ WHITE BLOB: พบปุ่มสีขาวกลมที่ [${whiteBox.centerX()}, ${whiteBox.centerY()}]")
            if (continuation.isActive) continuation.resume(Triple(whiteBox.centerX(), whiteBox.centerY(), "WHITE_BUTTON"))
            return@suspendCancellableCoroutine
        }

        // --- STEP 2: OCR PROCESS ---
        val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (continuation.isActive) {
                log("⏰ OCR TIMEOUT")
                continuation.resume(null)
            }
        }
        timeoutHandler.postDelayed(timeoutRunnable, 1500)

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                timeoutHandler.removeCallbacks(timeoutRunnable)
                
                // 2.1 ตรวจสอบหยุดบอท (เป๋าเต็ม / แจ้งเตือนกระเป๋าเต็ม)
                var hasAlertPopup = false
                
                for (block in visionText.textBlocks) {
                    val rawText = block.text.lowercase()
                    if (rawText.contains("alert")) {
                        hasAlertPopup = true
                    }
                }
                
                if (hasAlertPopup) {
                    log("⚠️ ตรวจพบป้าย Alert! คาดว่าเป็นกระเป๋าเต็ม...")
                    val blueBox = findBlueButton(pixels, w, h)
                    if (blueBox != null) {
                        log("⚠️ กำลังกดปุ่มตกลงสีฟ้า และสั่งหยุดบอททันที!")
                        if (continuation.isActive) continuation.resume(Triple(blueBox.centerX(), blueBox.centerY(), "BAG_FULL"))
                    } else {
                        if (continuation.isActive) continuation.resume(Triple(0, 0, "BAG_FULL"))
                    }
                    return@addOnSuccessListener
                }


                // 2.2 ค้นหาสัญลักษณ์ตัวอักษร หรือ ตัวเลข
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val text = element.text.trim()
                            val box = element.boundingBox
                            
                            // โหมดตกปลา: สัญลักษณ์ทั่วไป
                            if (box != null && text.isNotBlank() && text.length in 1..3) {
                                val aspectRatio = box.width().toFloat() / box.height().toFloat()
                                if (aspectRatio in 0.3f..3.0f) {
                                    log("🎯 เจอตัวสัญลักษณ์: '$text'")
                                    if (continuation.isActive) continuation.resume(Triple(box.centerX(), box.centerY(), text))
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                }

                // หยุดการหาเริ่มใหม่ถ้าเป็นตกปลา (ข้ามไป OCR ต่อ)

                // 2.3 ค้นหาข้อความเริ่มใหม่ / พลาด
                for (block in visionText.textBlocks) {
                    val bText = block.text
                    if (RESTART_TEXT_TARGETS.any { bText.contains(it) } || bText.contains("ใหม่") || bText.contains("พลาด")) {
                        for (line in block.lines) {
                            for (element in line.elements) {
                                val elText = element.text
                                if (elText.contains("เริ่ม") || elText.contains("ใหม่") || elText.contains("ตกลง")) {
                                    val box = element.boundingBox
                                    if (box != null) {
                                        log("🔄 พบข้อความเริ่มใหม่...")
                                        if (continuation.isActive) continuation.resume(Triple(box.centerX(), box.centerY(), "RESTART"))
                                        return@addOnSuccessListener
                                    }
                                }
                            }
                        }
                    }
                }

                // 2.4 ค้นหาสีฟ้า (ปุ่มเริ่มใหม่ Fallback)
                val blueBox = findBlueButton(pixels, w, h)
                if (blueBox != null) {
                    log("🔵 พบปุ่มเริ่มใหม่สีฟ้า...")
                    if (continuation.isActive) continuation.resume(Triple(blueBox.centerX(), blueBox.centerY(), "RESTART"))
                    return@addOnSuccessListener
                }

                if (continuation.isActive) continuation.resume(null)
            }
            .addOnFailureListener {
                timeoutHandler.removeCallbacks(timeoutRunnable)
                if (continuation.isActive) continuation.resume(null)
            }
    }

    private fun findWhiteButton(pixels: IntArray, w: Int, h: Int): Rect? {
        // สแกนหาพื้นที่สีขาวจัด (ปุ่มตกปลา)
        val startY = (h * 0.15).toInt()
        val endY = (h * 0.85).toInt()
        val startX = (w * 0.15).toInt()
        val endX = (w * 0.85).toInt()
        
        for (y in startY until endY step 10) {
            val rowOffset = y * w
            for (x in startX until endX step 10) {
                val pixel = pixels[rowOffset + x]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                
                // สีขาวจัด (R, G, B > 240) -> ตรวจสอบหา Blob หนาแน่น
                if (r > 240 && g > 240 && b > 240) {
                    var solidCount = 0
                    // ตรวจสอบในตารางรอบๆ รัศมี 15px
                    for (checkY in y - 15..y + 15 step 5) {
                        for (checkX in x - 15..x + 15 step 5) {
                            if (checkX in 0 until w && checkY in 0 until h) {
                                val cp = pixels[checkY * w + checkX]
                                if (((cp shr 16) and 0xff) > 210 && ((cp shr 8) and 0xff) > 210 && (cp and 0xff) > 210) {
                                    solidCount++
                                }
                            }
                        }
                    }
                    if (solidCount >= 32) {
                        return Rect(x - 30, y - 30, x + 30, y + 30)
                    }
                }
            }
        }
        return null
    }

    private fun findBlueButton(pixels: IntArray, w: Int, h: Int): Rect? {
        val startY = (h * 0.3).toInt()
        val endY = (h * 0.8).toInt()
        val startX = (w * 0.3).toInt()
        val endX = (w * 0.7).toInt()
        
        for (y in startY until endY step 15) {
            val rowOffset = y * w
            for (x in startX until endX step 15) {
                val pixel = pixels[rowOffset + x]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                if (b > 180 && b > r + 40 && b > g - 20) {
                    return Rect(x - 50, y - 30, x + 50, y + 30)
                }
            }
        }
        return null
    }
}
