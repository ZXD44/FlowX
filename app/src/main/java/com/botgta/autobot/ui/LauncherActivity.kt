package com.botgta.autobot.ui

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.botgta.autobot.services.FloatingMenuService
import com.botgta.autobot.ui.screens.*
import com.botgta.autobot.ui.components.*
import com.botgta.autobot.data.SecureStorage
import com.botgta.autobot.core.BotCore
import com.botgta.autobot.core.UpdateManager
import com.botgta.autobot.data.TranslationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class LauncherActivity : ComponentActivity() {

    companion object {
        private const val TAG = "LauncherActivity"
    }

    private var isOverlayGranted by mutableStateOf(false)
    private var isAccessibilityGranted by mutableStateOf(false)
    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { refreshPermissionStatus() }
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshPermissionStatus() }

    private var pendingAdRewardType by mutableIntStateOf(0) 
    private var adLaunchTime = 0L
    private var hasLeftApp = false
    private var expirationMs by mutableLongStateOf(0L)
    private var notificationMessage by mutableStateOf<String?>(null)
    private var notificationType by mutableStateOf("success")

    private fun packageVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) { "1.0.0" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SecureStorage.init(this)
        handleDeepLinkReward(intent)
        
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        expirationMs = SecureStorage.getExpirationTimestamp()
        val appLang = SecureStorage.getLanguage()
        TranslationManager.currentLang = if (appLang == "EN") TranslationManager.Lang.EN else TranslationManager.Lang.TH
        
        BotCore.showScanArea = SecureStorage.getShowScanArea()
        requestNotificationPermissionIfNeeded()
        
        intent.getStringExtra("DEEP_LINK_MSG")?.let { showDoodleNotif(it, "success") }
        
        setContent {
            val t = TranslationManager
            var isDarkMode by remember { mutableStateOf(SecureStorage.isDarkMode()) }
            var currentTab by remember { mutableIntStateOf(0) }
            var selectedJob by remember { mutableStateOf(SecureStorage.getSelectedJob()) }
            var isBotActive by remember { mutableStateOf(FloatingMenuService.isServiceRunning) }
            var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
            val tokenTimeSeconds = if (SecureStorage.isAuthorized() && SecureStorage.isAdminUnlimitedEnabled()) 999999999L else if (expirationMs > nowMs) (expirationMs - nowMs) / 1000L else 0L
            val version = packageVersionName(this)
            var latestVersion by remember { mutableStateOf("...") }
            var changelog by remember { mutableStateOf("") }
            var downloadUrl by remember { mutableStateOf("") }
            var showUpdateDialog by remember { mutableStateOf(false) }
            var isUpdateRequired by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableFloatStateOf(0f) }
            var webVisits by remember { mutableIntStateOf(SecureStorage.getWebsiteVisitsToday()) }

            LaunchedEffect(Unit) {
                try {
                    UpdateManager.checkForUpdate(this@LauncherActivity) { newVer, newLog, newUrl ->
                        latestVersion = newVer; changelog = newLog; downloadUrl = newUrl; showUpdateDialog = true; isUpdateRequired = true
                    }
                } catch (e: Exception) { }
            }

            LaunchedEffect(Unit) {
                while(true) {
                    delay(1000); nowMs = System.currentTimeMillis(); isBotActive = FloatingMenuService.isServiceRunning
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                BotDashboardScreen(
                    version = version, latestVersion = latestVersion, isOverlayGranted = isOverlayGranted, isAccessibilityGranted = isAccessibilityGranted, currentTab = currentTab, selectedJob = selectedJob, tokenTimeSeconds = tokenTimeSeconds, isBotActive = isBotActive,
                    onTabChange = { currentTab = it },
                    onJobSelect = { selectedJob = it; SecureStorage.setSelectedJob(it) },
                    onEnterKey = { key ->
                        val upperKey = key.uppercase().trim(); val now = System.currentTimeMillis(); val lastReward = SecureStorage.getLastRewardTimestamp()
                        if (upperKey.startsWith("ZXD-VIP-")) {
                            val providedCode = upperKey.removePrefix("ZXD-VIP-").toIntOrNull()
                            val calendar = java.util.Calendar.getInstance()
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            val month = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
                            val expectedCode = (day + month + 123) * 7
                            if (providedCode == expectedCode) {
                                if (now - lastReward < 3600_000) { notificationMessage = t.getString("notif_key_cooldown"); notificationType = "info" } 
                                else {
                                    val cur = SecureStorage.getTokenRemainingSeconds()
                                    if (cur >= 86400L) { notificationMessage = t.getString("notif_mana_full"); notificationType = "info" } 
                                    else {
                                        SecureStorage.setTokenRemainingSeconds((cur + 43200L).coerceAtMost(86400L))
                                        expirationMs = SecureStorage.getExpirationTimestamp()
                                        SecureStorage.setLastRewardTimestamp(now); notificationMessage = t.getString("notif_key_activated"); notificationType = "success"
                                    }
                                }
                            } else { notificationMessage = t.getString("notif_key_invalid"); notificationType = "error" }
                        } else if (upperKey == "ADMIN-RECOVER") {
                            SecureStorage.setTokenRemainingSeconds(10800L)
                            expirationMs = SecureStorage.getExpirationTimestamp()
                            notificationMessage = t.getString("notif_recovered"); notificationType = "info"
                        } else { notificationMessage = t.getString("notif_key_format"); notificationType = "error" }
                        lifecycleScope.launch { delay(3000); notificationMessage = null }
                    },
                    onGetFreeKey = { 
                        adLaunchTime = System.currentTimeMillis(); 
                        pendingAdRewardType = 1; 
                        hasLeftApp = false
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shrinkme.io/st?api=3a725361d627cc003559a17a2b6d1202b3f61d9f&url=https://www.zirconx.my"))) 
                    },
                    onShowGoogleAds = {
                        val currentVisits = SecureStorage.getWebsiteVisitsToday()
                        if (currentVisits >= 3) { showDoodleNotif(t.getString("notif_limit_reached"), "error") } 
                        else { 
                            val intent = Intent(this@LauncherActivity, RewardActivity::class.java)
                            intent.putExtra("TARGET_URL", "https://www.zirconx.my")
                            startActivity(intent)
                        }
                    },
                    onGrantOverlay = { overlayPermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) },
                    onGrantAccessibility = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onLaunchBot = {
                        if (isBotActive) {
                            stopService(Intent(this@LauncherActivity, FloatingMenuService::class.java))
                            notificationMessage = t.getString("notif_bot_stopped"); notificationType = "success"
                        } else {
                            if (isUpdateRequired) { notificationMessage = t.getString("notif_update_required"); notificationType = "error"; showUpdateDialog = true } 
                            else if (!isOverlayGranted || !isAccessibilityGranted) { notificationMessage = t.getString("notif_grant_permission"); notificationType = "error" } 
                            else {
                                val hasTime = SecureStorage.getTokenRemainingSeconds() > 0
                                val isUnlimitedAdmin = com.botgta.autobot.data.SecurityUtils.isDeviceAuthorized(this@LauncherActivity) && SecureStorage.isAdminUnlimitedEnabled()
                                
                                if (!hasTime && !isUnlimitedAdmin) {
                                    notificationMessage = t.getString("log_no_mana"); notificationType = "error"
                                } else {
                                    val intent = Intent(this@LauncherActivity, FloatingMenuService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
                                    notificationMessage = t.getString("notif_bot_started"); notificationType = "success"
                                }
                            }
                        }
                        lifecycleScope.launch { delay(3000); notificationMessage = null }
                    },
                    onCheckUpdate = { 
                        if (latestVersion != version && latestVersion != "...") {
                            showUpdateDialog = true 
                        } else { 
                            notificationMessage = t.getString("update_latest")
                            notificationType = "success"
                        }
                        lifecycleScope.launch { delay(3000); notificationMessage = null }
                    },
                    isDarkMode = isDarkMode, 
                    onDarkModeChange = { 
                        isDarkMode = it
                        SecureStorage.setDarkMode(it)
                        // Stop bot service if running to reset overlay colors
                        if (FloatingMenuService.isServiceRunning) {
                            stopService(Intent(this@LauncherActivity, FloatingMenuService::class.java))
                            isBotActive = false
                            notificationMessage = t.getString("notif_bot_stopped")
                            notificationType = "info"
                            lifecycleScope.launch { delay(3000); notificationMessage = null }
                        }
                    },
                    webVisits = webVisits,
                    isAuthorized = com.botgta.autobot.data.SecurityUtils.isDeviceAuthorized(this@LauncherActivity), isUpdateRequired = isUpdateRequired
                )

                Box(modifier = Modifier.fillMaxSize().padding(top = 50.dp), contentAlignment = Alignment.TopCenter) {
                    AnimatedNotification(message = notificationMessage, type = notificationType, isDarkMode = isDarkMode)
                }

                if (showUpdateDialog) {
                    BeautifulUpdateDialog(version = latestVersion, changelog = changelog, downloadProgress = downloadProgress, isDarkMode = isDarkMode, onUpdate = { UpdateManager.startDownload(this@LauncherActivity, downloadUrl, "FlowX_Update.apk") { downloadProgress = it } }, onDismiss = { if (isUpdateRequired) finish() else showUpdateDialog = false }, isForceUpdate = isUpdateRequired)
                }
            }
        }
    }

    private fun showDoodleNotif(message: String, type: String = "success") {
        notificationMessage = message
        notificationType = type
        lifecycleScope.launch {
            delay(3000)
            if (notificationMessage == message) notificationMessage = null
        }
    }

    override fun onPause() {
        super.onPause()
        if (pendingAdRewardType != 0) {
            hasLeftApp = true // ยืนยันว่าผู้ใช้สลับออกจากแอปไปจริงๆ (ไปดูโฆษณา)
        }
    }

    override fun onResume() { 
        super.onResume()
        refreshPermissionStatus()
        expirationMs = SecureStorage.getExpirationTimestamp()
        
        val t = TranslationManager
        val now = System.currentTimeMillis()
        val elapsed = now - adLaunchTime

        // 🏆 Handle ShrinkMe Reward (Type 1) - ระบบตรวจสอบความปลอดภัย
        if (pendingAdRewardType == 1 && adLaunchTime > 0) {
            // ต้องสลับออกจากแอปไปจริงๆ และใช้เวลาข้างนอกอย่างน้อย 20 วินาที
            if (hasLeftApp && elapsed > 20_000) { 
                pendingAdRewardType = 0
                hasLeftApp = false
                val cur = SecureStorage.getTokenRemainingSeconds()
                SecureStorage.setTokenRemainingSeconds((cur + 3600L).coerceAtMost(32400L))
                expirationMs = SecureStorage.getExpirationTimestamp()
                SecureStorage.setLastRewardTimestamp(now)
                showDoodleNotif("บันทึกรางวัล 1 ชม. สำเร็จ!", "success")
            } else if (!hasLeftApp && elapsed > 20_000) {
                // ถ้าผ่านไป 20 วิ แต่ไม่เคยสลับแอปเลย แสดงว่านั่งรอในแอปเพื่อโกง
                pendingAdRewardType = 0
                showDoodleNotif("กรุณาดูโฆษณาให้จบเพื่อรับรางวัล", "error")
            }
        }
    }
    private fun refreshPermissionStatus() { isOverlayGranted = Settings.canDrawOverlays(this); isAccessibilityGranted = isAccessibilityServiceEnabled() }
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC).any { it.resolveInfo.serviceInfo.packageName == packageName }
    }
    private fun requestNotificationPermissionIfNeeded() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); handleDeepLinkReward(intent) }
    private fun handleDeepLinkReward(intent: Intent?) {
        val t = TranslationManager
        val uri = intent?.data ?: return
        val host = uri.host ?: ""
        
        // ยอมรับทั้ง zirconx.my, www.zirconx.my และลิงก์รางวัลเดิม
        val isOurWeb = host == "zirconx.my" || host == "www.zirconx.my" || host == "reward"

        if (isOurWeb) {
            SecureStorage.init(this)
            val now = System.currentTimeMillis()
            val lastReward = SecureStorage.getLastRewardTimestamp()
            
            // ป้องกันการกดซ้ำเร็วเกินไป (1 นาที)
            if (now - lastReward < 60_000) return
            
            val cur = SecureStorage.getTokenRemainingSeconds()
            SecureStorage.setTokenRemainingSeconds((cur + 10800L).coerceAtMost(32400L))
            expirationMs = SecureStorage.getExpirationTimestamp()
            SecureStorage.setLastRewardTimestamp(now)
            pendingAdRewardType = 0 
            
            // แจ้งเตือนสไตล์ Doodle
            showDoodleNotif(t.getString("notif_success_ad"), "success")
        }
    }
}
