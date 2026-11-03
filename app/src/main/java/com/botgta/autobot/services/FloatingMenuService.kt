package com.botgta.autobot.services

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationCompat
import com.botgta.autobot.R
import com.botgta.autobot.core.BotCore
import com.botgta.autobot.databinding.LayoutOverlayBinding
import com.botgta.autobot.data.TranslationManager
import com.botgta.autobot.data.SecureStorage
import kotlinx.coroutines.*
import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * FloatingMenuService — UI Doodle Theme with Hand-drawn Quest Stamp
 */
class FloatingMenuService : Service() {

    companion object {
        private const val TAG = "OverlayService"
        private const val CHANNEL_ID = "overlay_channel"
        private const val NOTIFICATION_ID = 1001
        var isServiceRunning = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var binding: LayoutOverlayBinding
    private var isExpanded = false
    
    private var paperColor = 0xFFFFFFFF.toInt()
    private var inkColor = 0xFF1A1A1A.toInt()
    private var highlightColor = 0xFFFDE047.toInt()
    private val errorColor = 0xFFEF4444.toInt()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        SecureStorage.init(this)
        val lang = SecureStorage.getLanguage()
        TranslationManager.currentLang = if (lang == "EN") TranslationManager.Lang.EN else TranslationManager.Lang.TH
        
        val isDarkMode = SecureStorage.isDarkMode()
        highlightColor = if (isDarkMode) 0xFFA855F7.toInt() else 0xFFFDE047.toInt()
        paperColor = if (isDarkMode) 0xFF1A1A1A.toInt() else 0xFFFFFFFF.toInt()
        inkColor = if (isDarkMode) 0xFFE0E0E0.toInt() else 0xFF1A1A1A.toInt()

        BotCore.isServiceActive = true
        isServiceRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        createOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        BotCore.isServiceActive = false
        isServiceRunning = false
        removeOverlayView()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.overlay_channel_name), NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val t = TranslationManager
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(t.getString("overlay_title"))
            .setContentText(t.getString("overlay_running"))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(highlightColor)
            .build()
    }

    private fun createOverlayView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        binding = LayoutOverlayBinding.inflate(LayoutInflater.from(this))
        overlayView = binding.root
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 100; y = 200 }
        windowManager?.addView(overlayView, params)
        applyDoodleTheme()
        setupOverlayInteractions(params)
    }

    private fun applyDoodleTheme() {
        val t = TranslationManager
        // FAB (Collapsed) uses the "Signature Stamp" mode
        binding.fabCollapsed.background = DoodleDrawable(isStamp = true, bgColor = highlightColor, inkColor = inkColor)
        // Panel (Expanded) uses the "Diary Page" mode
        binding.panelExpanded.background = DoodleDrawable(isStamp = false, bgColor = paperColor, inkColor = inkColor)
        
        // Header
        binding.tvOverlayTitle.setTextColor(inkColor)
        binding.btnInfo.setColorFilter(inkColor)
        binding.btnCollapse.setTextColor(inkColor)

        // Status
        binding.tvOverlayStatus.setTextColor(inkColor)
        
        // Controls
        binding.btnOverlayStart.setBackgroundColor(highlightColor)
        binding.btnOverlayStart.setTextColor(if (SecureStorage.isDarkMode()) Color.WHITE else inkColor)
        binding.btnOverlayStop.setBackgroundColor(errorColor)
        binding.btnOverlayStop.setTextColor(Color.WHITE)
        
        // Website & Footer
        binding.btnWebsite.setTextColor(inkColor)
        binding.tvOverlayFooter.setTextColor(inkColor)

        binding.btnOverlayStart.text = t.getString("btn_deploy")
        binding.tvOverlayStatus.text = t.getString("overlay_ready")
        binding.btnOverlayStop.text = t.getString("btn_recall")
    }

    private fun setupOverlayInteractions(params: WindowManager.LayoutParams) {
        binding.fabCollapsed.setOnClickListener { togglePanel(true) }
        binding.btnCollapse.setOnClickListener { togglePanel(false) }
        var initialX = 0; var initialY = 0; var initialTouchX = 0f; var initialTouchY = 0f; var isDragging = false
        val dragTouchListener = View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { initialX = params.x; initialY = params.y; initialTouchX = event.rawX; initialTouchY = event.rawY; isDragging = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt(); val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) isDragging = true
                    params.x = initialX + dx; params.y = initialY + dy
                    windowManager?.updateViewLayout(overlayView, params); true
                }
                MotionEvent.ACTION_UP -> { if (!isDragging) v.performClick(); true }
                else -> false
            }
        }
        binding.fabCollapsed.setOnTouchListener(dragTouchListener); binding.dragHandle.setOnTouchListener(dragTouchListener)
        binding.btnOverlayStart.setOnClickListener { onBotStart() }; binding.btnOverlayStop.setOnClickListener { onBotStop() }
        binding.btnInfo.setOnClickListener { binding.panelInfo.visibility = if (binding.panelInfo.visibility == View.VISIBLE) View.GONE else View.VISIBLE }
        binding.btnWebsite.setOnClickListener { try { val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.zirconx.my/")); intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK; startActivity(intent) } catch (e: Exception) { } }
    }

    private fun togglePanel(expand: Boolean) {
        isExpanded = expand
        if (expand) { binding.fabCollapsed.visibility = View.GONE; binding.panelExpanded.visibility = View.VISIBLE } 
        else { binding.fabCollapsed.visibility = View.VISIBLE; binding.panelExpanded.visibility = View.GONE }
    }

    private var scannerView: View? = null; private var timerJob: kotlinx.coroutines.Job? = null

    private fun addLog(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        Log.d(TAG, "[$time] $msg")
    }

    private fun onBotStart() {
        val t = TranslationManager
        val timeRemaining = SecureStorage.getTokenRemainingSeconds()
        val isAuthorized = com.botgta.autobot.data.SecurityUtils.isDeviceAuthorized(this)
        if (timeRemaining <= 0 && !isAuthorized) { addLog(t.getString("log_no_mana")); return }
        binding.btnOverlayStart.visibility = View.GONE; binding.btnOverlayStop.visibility = View.VISIBLE
        binding.tvOverlayStatus.text = t.getString("overlay_running"); binding.tvOverlayStatus.setTextColor(inkColor)
        addLog(t.getString("log_start"))
        com.botgta.autobot.logic.MiningManager.onLog = { msg -> addLog(msg) }
        com.botgta.autobot.logic.FishingManager.onLog = { msg -> addLog(msg) }
        showScannerFrame(); BotCore.start(this)
    }

    private fun onBotStop() {
        val t = TranslationManager
        binding.btnOverlayStart.visibility = View.VISIBLE; binding.btnOverlayStop.visibility = View.GONE
        binding.tvOverlayStatus.text = t.getString("overlay_ready"); binding.tvOverlayStatus.setTextColor(inkColor)
        addLog(t.getString("log_stop"))
        com.botgta.autobot.logic.MiningManager.onLog = null; com.botgta.autobot.logic.FishingManager.onLog = null
        hideScannerFrame(); BotCore.stop()
    }

    private fun showScannerFrame() {
        if (scannerView != null) return
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        val layoutRes = if (BotCore.isMiningMode) R.layout.layout_scanner_frame_mining else R.layout.layout_scanner_frame_fishing
        val view = LayoutInflater.from(this).inflate(layoutRes, null)
        
        // Theme HUD Panel
        val isDarkMode = SecureStorage.isDarkMode()
        val hudPanel = view.findViewById<View>(R.id.hudPanel)
        val tvScanStatus = view.findViewById<android.widget.TextView>(R.id.tvScanStatus)
        if (isDarkMode) {
            hudPanel?.setBackgroundColor(paperColor)
            tvScanStatus?.setTextColor(highlightColor)
        } else {
            hudPanel?.setBackgroundColor(0xFFFDE047.toInt())
            tvScanStatus?.setTextColor(0xFF1A1A1A.toInt())
        }

        updateOverlayTimer(view)
        view.findViewById<View>(R.id.roiRedBox)?.visibility = if (BotCore.showScanArea) View.VISIBLE else View.INVISIBLE
        windowManager?.addView(view, params); scannerView = view
    }

    private fun updateOverlayTimer(view: View) {
        val t = TranslationManager
        val tvScanStatus = view.findViewById<android.widget.TextView>(R.id.tvScanStatus)
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val isAuthorized = com.botgta.autobot.data.SecurityUtils.isDeviceAuthorized(this@FloatingMenuService)
                val tokenSecs = SecureStorage.getTokenRemainingSeconds()
                val tokenTimeStr = if (isAuthorized || tokenSecs > 10000000L) t.getString("mana_unlimited") else {
                    val hrs = tokenSecs / 3600; val mins = (tokenSecs % 3600) / 60; val secs = tokenSecs % 60
                    String.format("%02d:%02d:%02d", hrs, mins, secs)
                }
                tvScanStatus.text = t.getString("mana_label") + tokenTimeStr
                
                val isRunning = binding.tvOverlayStatus.text == t.getString("overlay_running")
                
                if (tokenSecs <= 0 && !isAuthorized && isRunning) {
                    onBotStop()
                    binding.tvOverlayStatus.text = t.getString("log_no_mana")
                    binding.tvOverlayStatus.setTextColor(errorColor)
                }
                delay(1000)
            }
        }
    }

    private fun hideScannerFrame() { timerJob?.cancel(); timerJob = null; scannerView?.let { try { windowManager?.removeView(it) } catch (e: Exception) { } }; scannerView = null }
    private fun removeOverlayView() { hideScannerFrame(); overlayView?.let { try { windowManager?.removeView(it) } catch (e: Exception) { } }; overlayView = null; windowManager = null }

    /**
     * DoodleDrawable: ตกแต่งใหม่สไตล์ "ตราประทับเควส" (Quest Stamp)
     */
    class DoodleDrawable(private val isStamp: Boolean, private val bgColor: Int, private val inkColor: Int) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val strokeWidth = 8f
        private val shadowOffset = 8f
        private val cornerRadius = 8f

        private val rect = RectF()
        private val shadowRect = RectF()
        private val stampPath = Path()

        init {
            paint.color = Color.parseColor("#44000000")
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            rect.set(bounds)
            shadowRect.set(bounds)
            shadowRect.offset(shadowOffset, shadowOffset)
            
            if (isStamp) {
                stampPath.reset()
                val margin = rect.width() * 0.25f
                val cx = rect.centerX()
                val cy = rect.centerY()
                
                stampPath.moveTo(cx - margin, cy - margin)
                stampPath.lineTo(cx + margin, cy - margin)
                stampPath.lineTo(cx - margin, cy + margin)
                stampPath.lineTo(cx + margin, cy + margin)
            }
        }

        override fun draw(canvas: Canvas) {
            // 1. Draw Solid "Hand-drawn" Shadow
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#44000000")
            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, paint)

            // 2. Draw Paper Background
            paint.color = bgColor
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            // 3. Draw Thick Ink Border
            paint.style = Paint.Style.STROKE
            paint.color = inkColor
            paint.strokeWidth = strokeWidth
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            // 4. Decorate the "Quest Stamp" (Collapsed Button Only)
            if (isStamp) {
                drawStampDecoration(canvas)
            }
        }

        private fun drawStampDecoration(canvas: Canvas) {
            paint.style = Paint.Style.STROKE
            paint.color = inkColor
            paint.strokeWidth = 6f
            paint.strokeCap = Paint.Cap.ROUND
            
            canvas.drawPath(stampPath, paint)
            
            // Add doodle hatch marks on the corners
            paint.strokeWidth = 3f
            canvas.drawLine(rect.left + 15, rect.top + 15, rect.left + 30, rect.top + 5, paint)
            canvas.drawLine(rect.right - 15, rect.bottom - 15, rect.right - 30, rect.bottom - 5, paint)
        }

        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
