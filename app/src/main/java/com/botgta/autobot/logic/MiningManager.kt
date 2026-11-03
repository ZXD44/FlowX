package com.botgta.autobot.logic

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/**
 * MiningManager V13 — Fast Minigame + Early Termination OCR
 */
object MiningManager {

    private const val TAG = "MiningBrain"
    // ROI = กรอบสีแดงที่ผู้ใช้กำหนด: x 28-78%, y 34-83% ของหน้าจอ
    private const val ROI_W_PERCENT = 0.50f   // กว้าง 50% ของจอ (28% - 78%)
    private const val ROI_H_PERCENT = 0.49f   // สูง 49% ของจอ (34% - 83%)
    private const val ROI_Y_BIAS = 0.67f      // roiY = (fullH - roiH) * 0.67 ≈ fullH * 0.34
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class MiningResult(val x: Float, val y: Float, val label: String)

    var onLog: ((String) -> Unit)? = null

    private fun log(msg: String) {
        Log.d(TAG, msg)
        onLog?.invoke(msg)
    }

    suspend fun processFrameAndClick(
        frame: Bitmap,
        speedMultiplier: Float,
        useHumanization: Boolean
    ): com.botgta.autobot.core.BotResult? {
        val detection = analyzeMiningFrame(frame)
        frame.recycle()
        
        if (detection == null) return null
        
        var finalX = detection.x
        var finalY = detection.y
        val label = detection.label
        val isMinigame = label.startsWith("NUM_")
        
        if (label == "BAG_FULL_STOP") return com.botgta.autobot.core.BotResult(label, true)
        
        // Humanization
        if (useHumanization) {
            val r = if (isMinigame) 3 else 12
            finalX += kotlin.random.Random.nextInt(-r, r + 1)
            finalY += kotlin.random.Random.nextInt(-r, r + 1)
        }
        
        val delayTime = if (isMinigame) {
            (kotlin.random.Random.nextLong(80, 150) * speedMultiplier).toLong()
        } else {
            (kotlin.random.Random.nextLong(15, 40) * speedMultiplier).toLong()
        }
        
        kotlinx.coroutines.delay(delayTime)
        
        val controlService = com.botgta.autobot.services.GestureControlService.instance
        if (controlService != null) {
            controlService.performTap(finalX, finalY)
        }
        
        return com.botgta.autobot.core.BotResult(label, false)
    }

    suspend fun analyzeMiningFrame(fullBitmap: Bitmap): MiningResult? = withContext(Dispatchers.Default) {
        var roiBitmap: Bitmap? = null
        try {
            val fullW = fullBitmap.width
            val fullH = fullBitmap.height

            val roiW = (fullW * ROI_W_PERCENT).toInt()
            val roiH = (fullH * ROI_H_PERCENT).toInt()
            val roiX = (fullW - roiW) / 2
            val spaceY = fullH - roiH
            val roiY = (spaceY * ROI_Y_BIAS).toInt()

            roiBitmap = Bitmap.createBitmap(fullBitmap, roiX, roiY, roiW, roiH)
            
            val pixels = IntArray(roiW * roiH)
            roiBitmap.getPixels(pixels, 0, roiW, 0, 0, roiW, roiH)

            // 1. เช็คปุ่มสีฟ้า "ตกลง" บน Alert popup (เช่น "กระเป๋าไม่สามารถบรรจุไอเทมนี้เพิ่มได้อีก")
            val blueBtn = findBlueButton(pixels, roiW, roiH)
            if (blueBtn != null) {
                log("🔵 พบ Alert popup + ปุ่มตกลงสีฟ้า -> กดเพื่อปิด popup")
                return@withContext MiningResult((blueBtn.centerX() + roiX).toFloat(), (blueBtn.centerY() + roiY).toFloat(), "ALERT_OK")
            }

            // 2. เช็คระบบขุด/มินิเกม
            if (checkMinigameContainer(pixels, roiW, roiH)) {
                log("🎮 เข้าสู่โหมดมินิเกม (V13 Fast OCR)...")
                
                val yellowPoint = findYellowPixels(pixels, roiW, roiH)
                if (yellowPoint == null) {
                    log("⌛ กำลังสแกนหาเลขเป้าหมาย...")
                    return@withContext null
                }

                // Double-read เลขเป้าหมายเพื่อยืนยันความถูกต้อง
                val targetCrop1 = createHighContrastCrop(roiBitmap, yellowPoint.first, yellowPoint.second, 75)
                val targetDigit1 = ocrSingleDigit(targetCrop1)
                targetCrop1?.recycle()

                if (targetDigit1 == null) {
                    log("🔍 สแกนเลขเป้าหมายไม่สำเร็จ (Retry...)")
                    return@withContext null
                }
                
                // อ่านซ้ำรอบ 2 เพื่อยืนยัน (ป้องกัน OCR อ่านผิด)
                val targetCrop2 = createHighContrastCrop(roiBitmap, yellowPoint.first, yellowPoint.second, 85)
                val targetDigit2 = ocrSingleDigit(targetCrop2)
                targetCrop2?.recycle()
                
                val targetDigit = if (targetDigit1 == targetDigit2) {
                    targetDigit1 // ยืนยันตรงกัน 100%
                } else {
                    log("⚠️ เลขเป้าหมายอ่านไม่ตรงกัน ($targetDigit1 vs $targetDigit2) -> ใช้ผลรอบแรก")
                    targetDigit1
                }
                log("🎯 เลขเป้าหมาย: $targetDigit (verified: ${targetDigit1 == targetDigit2})")

                val blobs = findWhiteCircleBlobs(pixels, roiW, roiH)
                log("🔘 พบปุ่มตัวเลข ${blobs.size} ปุ่ม เริ่มวิเคราะห์แบบขนาน...")

                // Parallel OCR with early termination, verification & timeout
                val matchResult = withTimeoutOrNull(1500L) {
                    val channel = kotlinx.coroutines.channels.Channel<Pair<Rect, String?>>(kotlinx.coroutines.channels.Channel.UNLIMITED)
                    val jobs = blobs.map { blob ->
                        launch {
                            val blobCrop = createHighContrastCrop(roiBitmap!!, blob.centerX(), blob.centerY(), 65)
                            val digit = ocrSingleDigit(blobCrop)
                            blobCrop?.recycle()
                            channel.send(Pair(blob, digit))
                        }
                    }
                    var found: MiningResult? = null
                    repeat(blobs.size) {
                        val (blob, digit) = channel.receive()
                        if (digit == targetDigit && found == null) {
                            // ✅ Verification: อ่านซ้ำปุ่มนี้ด้วย crop ขนาดต่างกันเพื่อยืนยัน
                            val verifyCrop = createHighContrastCrop(roiBitmap!!, blob.centerX(), blob.centerY(), 55)
                            val verifyDigit = ocrSingleDigit(verifyCrop)
                            verifyCrop?.recycle()
                            
                            if (verifyDigit == targetDigit) {
                                val gx = blob.centerX() + roiX
                                val gy = blob.centerY() + roiY
                                log("✅ Match+Verified! [เป้าหมาย $targetDigit == ปุ่ม $digit ✓$verifyDigit] -> กดที่ [$gx, $gy]")
                                found = MiningResult(gx.toFloat(), gy.toFloat(), "NUM_$targetDigit")
                                jobs.forEach { it.cancel() }
                                return@withTimeoutOrNull found
                            } else {
                                log("⚠️ ปุ่มอ่านได้ $digit แต่ยืนยันเป็น $verifyDigit -> ข้าม (ป้องกันกดผิด)")
                            }
                        }
                    }
                    channel.close()
                    found
                }
                if (matchResult != null) return@withContext matchResult
                
                log("🔍 ไม่พบเลข $targetDigit ในปุ่มที่สแกนเจอ (timeout/mismatch)")
                return@withContext null
            }

            // 3. ระบบหาไอคอน "กดเพื่อใช้งาน" (เริ่มขุด → รอมินิเกมเด้ง)
            val finger = findFingerIcon(pixels, roiW, roiH)
            if (finger != null) {
                val gx = finger.centerX() + roiX
                val gy = finger.centerY() + roiY
                log("👆 พบไอคอน 'กดเพื่อใช้งาน' -> กดเพื่อเริ่มขุด")
                return@withContext MiningResult(gx.toFloat(), gy.toFloat(), "FINGER")
            }

            return@withContext null
        } catch (e: Exception) {
            log("❌ Error in analyzeMiningFrame: ${e.message}")
            return@withContext null
        } finally {
            roiBitmap?.recycle()
        }
    }

    private suspend fun ocrSingleDigit(bmp: Bitmap?): String? = withContext(Dispatchers.Default) {
        if (bmp == null) return@withContext null
        suspendCancellableCoroutine { cont ->
            val inputImage = InputImage.fromBitmap(bmp, 0)
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    if (!cont.isActive) return@addOnSuccessListener
                    val digits = visionText.text.trim().filter { it.isDigit() }
                    if (digits.isNotEmpty()) {
                        cont.resume(digits.take(1))
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener { 
                    if (cont.isActive) cont.resume(null) 
                }
        }
    }

    private fun createHighContrastCrop(src: Bitmap, cx: Int, cy: Int, size: Int): Bitmap? {
        val left = (cx - size).coerceIn(0, src.width - 1)
        val top = (cy - size).coerceIn(0, src.height - 1)
        val right = (cx + size).coerceIn(0, src.width)
        val bottom = (cy + size).coerceIn(0, src.height)
        val w = right - left
        val h = bottom - top
        if (w <= 0 || h <= 0) return null

        val crop = Bitmap.createBitmap(src, left, top, w, h)
        val scale = 5
        val scaled = Bitmap.createScaledBitmap(crop, w * scale, h * scale, true)
        crop.recycle()
        return scaled
    }

    private fun findYellowPixels(pixels: IntArray, w: Int, h: Int): Pair<Int, Int>? {
        var sumX = 0L; var sumY = 0L; var count = 0
        var y = (h * 0.2).toInt()
        while (y < h * 0.7) {
            val rowOffset = y * w
            var x = (w * 0.25).toInt()
            while (x < (w * 0.75).toInt()) {
                val p = pixels[rowOffset + x]
                val r = (p shr 16) and 0xff; val g = (p shr 8) and 0xff; val b = p and 0xff
                
                if (r > 130 && g > 90 && b < 130 && r > b + 30) {
                    var darkPoints = 0
                    val offsets = intArrayOf(-25, 25)
                    for (dy in offsets) {
                        for (dx in offsets) {
                            val nx = x + dx; val ny = y + dy
                            if (nx in 0 until w && ny in 0 until h) {
                                val np = pixels[ny * w + nx]
                                if (((np shr 16) and 0xff) < 95 && ((np shr 8) and 0xff) < 95) darkPoints++
                            }
                        }
                    }
                    if (darkPoints >= 2) {
                        sumX += x; sumY += y; count++
                    }
                }
                x += 6
            }
            y += 6
        }
        return if (count >= 3) Pair((sumX / count).toInt(), (sumY / count).toInt()) else null
    }

    private fun findWhiteCircleBlobs(pixels: IntArray, w: Int, h: Int): List<Rect> {
        val candidates = mutableListOf<Pair<Int, Int>>()
        var y = (h * 0.35).toInt()
        while (y < h * 0.95) {
            val rowOffset = y * w
            var x = (w * 0.1).toInt()
            // ตัด UI ด้านขวาออก (ปุ่มวงกลม interaction) ลดจาก 0.9 เป็น 0.85
            while (x < (w * 0.85).toInt()) {
                val p = pixels[rowOffset + x]
                if (((p shr 16) and 0xff) > 200 && ((p shr 8) and 0xff) > 200 && (p and 0xff) > 200) {
                    candidates.add(Pair(x, y))
                }
                x += 20
            }
            y += 20
        }
        val blobs = mutableListOf<Rect>()
        val used = BooleanArray(candidates.size)
        for (i in candidates.indices) {
            if (used[i]) continue
            val group = mutableListOf(candidates[i]); used[i] = true
            for (j in i + 1 until candidates.size) {
                if (!used[j] && distSq(candidates[i].first, candidates[i].second, candidates[j].first, candidates[j].second) < 75 * 75) {
                    group.add(candidates[j]); used[j] = true
                }
            }
            // กรองเฉพาะ blob ที่มีจุดพอ (ปุ่มจริงจะมีหลายจุดสีขาว)
            if (group.size < 3) continue
            val cx = group.map { it.first }.average().toInt()
            val cy = group.map { it.second }.average().toInt()
            blobs.add(Rect(cx - 50, cy - 50, cx + 50, cy + 50))
        }
        return blobs
    }

    private fun distSq(x1: Int, y1: Int, x2: Int, y2: Int) = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)

    private fun checkMinigameContainer(pixels: IntArray, w: Int, h: Int): Boolean {
        var darkCount = 0
        val pts = listOf(Pair(w / 2, h / 2), Pair(w / 3, h / 2), Pair(2 * w / 3, h / 2))
        for (p in pts) {
            val px = pixels[p.second * w + p.first]
            if (((px shr 16) and 0xff) < 95 && ((px shr 8) and 0xff) < 95 && (px and 0xff) < 95) darkCount++
        }
        return darkCount >= 2
    }

    /**
     * หา Alert popup + ปุ่มสีฟ้า "ตกลง"
     * 1) สแกนหา cluster ของ pixel สีฟ้าสด (ปุ่ม "ตกลง")
     * 2) ยืนยัน popup context: ต้องมีพื้นหลังเทาเข้ม (Alert popup) อยู่เหนือปุ่ม
     * วิธีนี้กัน false positive จากพื้นหลัง/วัตถุสีฟ้าในเกมทั่วไป
     */
    private fun findBlueButton(pixels: IntArray, w: Int, h: Int): Rect? {
        // 1) เก็บ candidate pixel สีฟ้าสด
        val bluePoints = mutableListOf<Pair<Int, Int>>()
        var y = (h * 0.30).toInt()
        val yEnd = (h * 0.90).toInt()
        val xStart = (w * 0.20).toInt()
        val xEnd = (w * 0.85).toInt()
        while (y < yEnd) {
            val rowOffset = y * w
            var x = xStart
            while (x < xEnd) {
                val p = pixels[rowOffset + x]
                val r = (p shr 16) and 0xff
                val g = (p shr 8) and 0xff
                val b = p and 0xff
                // bright blue button: low R, mid G, high B, B clearly > R
                if (r < 130 && g in 110..210 && b > 180 && b > r + 60) {
                    bluePoints.add(Pair(x, y))
                }
                x += 6
            }
            y += 6
        }
        if (bluePoints.size < 25) return null

        // 2) หา cluster ที่ใหญ่ที่สุด (เลือก centroid ของกลุ่มหลัก)
        val cx = bluePoints.map { it.first }.average().toInt()
        val cy = bluePoints.map { it.second }.average().toInt()
        // กรอง outlier: เก็บเฉพาะจุดใกล้ centroid (radius ~80px)
        val core = bluePoints.filter {
            val dx = it.first - cx; val dy = it.second - cy
            dx * dx + dy * dy < 80 * 80
        }
        if (core.size < 20) return null
        val ccx = core.map { it.first }.average().toInt()
        val ccy = core.map { it.second }.average().toInt()

        // 3) ยืนยัน popup context: เหนือปุ่มต้องมีพื้นหลังเทาเข้ม (สี popup)
        // popup background ~ RGB (40-90, 40-90, 40-90) มีโทนเทาเข้ม
        var darkAbove = 0
        var sampled = 0
        val checkY1 = (ccy - 80).coerceAtLeast(0)
        val checkY2 = (ccy - 30).coerceAtLeast(0)
        val checkX1 = (ccx - 60).coerceAtLeast(0)
        val checkX2 = (ccx + 60).coerceAtMost(w - 1)
        var sy = checkY1
        while (sy < checkY2) {
            var sx = checkX1
            while (sx < checkX2) {
                val p = pixels[sy * w + sx]
                val r = (p shr 16) and 0xff
                val g = (p shr 8) and 0xff
                val b = p and 0xff
                // เทาเข้ม (popup) - RGB ใกล้กัน + ค่าต่ำ
                if (r in 30..100 && g in 30..100 && b in 30..110 &&
                    Math.abs(r - g) < 25 && Math.abs(g - b) < 30) {
                    darkAbove++
                }
                sampled++
                sx += 8
            }
            sy += 8
        }
        // ต้องมีพื้นหลังเทาเข้มเหนือปุ่ม >= 40% ของพื้นที่ตรวจสอบ
        if (sampled == 0 || darkAbove * 100 / sampled < 40) return null

        return Rect(ccx - 50, ccy - 20, ccx + 50, ccy + 20)
    }

    /**
     * ตรวจจับไอคอน "กดเพื่อใช้งาน" (วงกลม + รูปนิ้วมือสีขาว)
     * วิธี: หา grid cell ขนาด ~80x80 ที่มี pixel ขาวสว่างหนาแน่นที่สุด
     *       แล้วยืนยันว่าพื้นที่รอบๆ มืด (ไม่ใช่ UI สว่างทั่วไป)
     */
    private fun findFingerIcon(pixels: IntArray, w: Int, h: Int): Rect? {
        // สแกนเกือบทั้ง ROI (เผื่อ icon ปรากฏกลาง/ขวา) แต่เลี่ยงขอบบน-ล่างและขวาสุด (UI status bar)
        val x1 = (w * 0.20).toInt(); val x2 = (w * 0.92).toInt()
        val y1 = (h * 0.20).toInt(); val y2 = (h * 0.85).toInt()

        val cellSize = 80
        val step = 16
        var bestX = 0; var bestY = 0; var bestCount = 0
        var y = y1
        while (y < y2) {
            var x = x1
            while (x < x2) {
                // นับ pixel ขาวสว่างใน cell (x..x+cellSize, y..y+cellSize)
                var whiteCount = 0
                var sy = y
                val syEnd = (y + cellSize).coerceAtMost(h - 1)
                val sxEnd = (x + cellSize).coerceAtMost(w - 1)
                while (sy < syEnd) {
                    val rowOffset = sy * w
                    var sx = x
                    while (sx < sxEnd) {
                        val p = pixels[rowOffset + sx]
                        val r = (p shr 16) and 0xff
                        val g = (p shr 8) and 0xff
                        val b = p and 0xff
                        if (r > 220 && g > 220 && b > 220) whiteCount++
                        sx += 4
                    }
                    sy += 4
                }
                if (whiteCount > bestCount) {
                    bestCount = whiteCount
                    bestX = x + cellSize / 2
                    bestY = y + cellSize / 2
                }
                x += step
            }
            y += step
        }

        // ไอคอนนิ้วต้องมี white pixel หนาแน่นพอ (วงกลม+นิ้ว) - threshold ปรับตาม sample step 4
        // cell 80x80 sample ทุก 4px = ~400 จุด ไอคอนจริงจะติด ~30-80 จุด
        if (bestCount < 25) return null

        // ยืนยัน: รอบๆ ไอคอน (ขอบนอก) ต้องมีพื้นที่ไม่สว่าง (ไม่ใช่ UI panel ขาว/UI status)
        var darkRing = 0
        var ringSamples = 0
        val ringRadius = 70
        val angles = doubleArrayOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
        for (deg in angles) {
            val rad = Math.toRadians(deg)
            val nx = (bestX + ringRadius * Math.cos(rad)).toInt()
            val ny = (bestY + ringRadius * Math.sin(rad)).toInt()
            if (nx in 0 until w && ny in 0 until h) {
                val p = pixels[ny * w + nx]
                val avg = (((p shr 16) and 0xff) + ((p shr 8) and 0xff) + (p and 0xff)) / 3
                if (avg < 200) darkRing++  // ไม่ใช่สีขาวสว่าง
                ringSamples++
            }
        }
        // อย่างน้อย 50% ของรอบไอคอน ต้องไม่ขาว (กัน false positive จาก panel ขาวขนาดใหญ่)
        if (ringSamples == 0 || darkRing * 100 / ringSamples < 50) return null

        return Rect(bestX - 50, bestY - 50, bestX + 50, bestY + 50)
    }
}
