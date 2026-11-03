package com.botgta.autobot.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREFS_FILE = "bot_secure_prefs"
    private const val KEY_TOKEN_SECS = "token_remaining_seconds"
    private const val KEY_EXPIRATION_TIMESTAMP = "token_expiration_timestamp"
    private const val KEY_TOTAL_USED_SECS = "total_used_seconds"
    private const val KEY_ADS_WATCHED = "ads_watched_count"
    private const val KEY_SHOW_SCAN_AREA = "show_scan_area"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"
    private const val KEY_WEB_VISITS = "web_visits_count"
    private const val KEY_WEB_VISITS_DATE = "web_visits_date"
    private const val KEY_LAST_REWARD_TIME = "last_reward_timestamp"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_ADMIN_UNLIMITED = "admin_unlimited_time"

    
    private var sharedPrefs: SharedPreferences? = null
    private var isAuthorizedDevice: Boolean = false

    fun init(context: Context) {
        isAuthorizedDevice = SecurityUtils.isDeviceAuthorized(context)
        if (sharedPrefs == null) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                sharedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                sharedPrefs = context.getSharedPreferences("bot_fallback_prefs", Context.MODE_PRIVATE)
            }
        }
    }

    fun getWebsiteVisitsToday(): Int {
        val prefs = sharedPrefs ?: return 0
        val lastDate = prefs.getString(KEY_WEB_VISITS_DATE, "") ?: ""
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        if (lastDate != today) {
            prefs.edit().putInt(KEY_WEB_VISITS, 0).putString(KEY_WEB_VISITS_DATE, today).apply()
            return 0
        }
        return prefs.getInt(KEY_WEB_VISITS, 0)
    }

    fun incrementWebsiteVisitsToday() {
        val prefs = sharedPrefs ?: return
        val current = getWebsiteVisitsToday()
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        prefs.edit()
            .putInt(KEY_WEB_VISITS, current + 1)
            .putString(KEY_WEB_VISITS_DATE, today)
            .apply()
    }

    fun setTokenRemainingSeconds(seconds: Long, baseTime: Long = System.currentTimeMillis()) {
        val prefs = sharedPrefs ?: return
        val expirationTime = baseTime + (seconds * 1000L)
        prefs.edit()
            .putLong(KEY_TOKEN_SECS, seconds)
            .putLong(KEY_EXPIRATION_TIMESTAMP, expirationTime)
            .apply()
    }

    fun getTokenRemainingSeconds(externalCurrentTime: Long? = null): Long {
        if (isAuthorizedDevice && isAdminUnlimitedEnabled()) return 999999999L
        val prefs = sharedPrefs ?: return 0L
        val expirationTimestamp = prefs.getLong(KEY_EXPIRATION_TIMESTAMP, 0L)
        if (expirationTimestamp == 0L) return 0L
        val now = externalCurrentTime ?: System.currentTimeMillis()
        val remainingMillis = expirationTimestamp - now
        return if (remainingMillis > 0) remainingMillis / 1000L else 0L
    }

    fun getExpirationTimestamp(): Long = sharedPrefs?.getLong(KEY_EXPIRATION_TIMESTAMP, 0L) ?: 0L

    fun isAuthorized(): Boolean = isAuthorizedDevice

    fun isAdminUnlimitedEnabled(): Boolean = sharedPrefs?.getBoolean(KEY_ADMIN_UNLIMITED, true) ?: true

    fun setAdminUnlimited(enabled: Boolean) {
        sharedPrefs?.edit()?.putBoolean(KEY_ADMIN_UNLIMITED, enabled)?.apply()
    }

    fun isAuthorized(context: Context): Boolean {
        // อัปเดตค่า cache ด้วยถ้ามีการเช็คผ่าน context
        isAuthorizedDevice = com.botgta.autobot.data.SecurityUtils.isDeviceAuthorized(context)
        return isAuthorizedDevice
    }

    fun updateRunningTokenSeconds(seconds: Long) {
        val prefs = sharedPrefs ?: return
        val oldSecs = prefs.getLong(KEY_TOKEN_SECS, 0L)
        if (oldSecs > seconds) {
            val consumed = oldSecs - seconds
            val totalUsed = prefs.getLong(KEY_TOTAL_USED_SECS, 0L)
            prefs.edit().putLong(KEY_TOTAL_USED_SECS, totalUsed + consumed).apply()
        }
        prefs.edit().putLong(KEY_TOKEN_SECS, seconds).apply()
    }

    fun getTotalUsedSeconds(): Long = sharedPrefs?.getLong(KEY_TOTAL_USED_SECS, 0L) ?: 0L

    fun getAdsWatchedCount(): Int = sharedPrefs?.getInt(KEY_ADS_WATCHED, 0) ?: 0

    fun incrementAdsWatched() {
        val prefs = sharedPrefs ?: return
        val current = prefs.getInt(KEY_ADS_WATCHED, 0)
        prefs.edit().putInt(KEY_ADS_WATCHED, current + 1).apply()
    }

    fun getShowScanArea(): Boolean = sharedPrefs?.getBoolean(KEY_SHOW_SCAN_AREA, true) ?: true

    fun setShowScanArea(show: Boolean) {
        sharedPrefs?.edit()?.putBoolean(KEY_SHOW_SCAN_AREA, show)?.apply()
    }

    fun isDarkMode(): Boolean = sharedPrefs?.getBoolean(KEY_IS_DARK_MODE, false) ?: false

    fun setDarkMode(isDark: Boolean) {
        sharedPrefs?.edit()?.putBoolean(KEY_IS_DARK_MODE, isDark)?.apply()
    }

    fun getSelectedJob(): String = sharedPrefs?.getString("selected_job", "ตกปลา") ?: "ตกปลา"

    fun setSelectedJob(job: String) {
        sharedPrefs?.edit()?.putString("selected_job", job)?.apply()
    }

    fun getLastRewardTimestamp(): Long = sharedPrefs?.getLong(KEY_LAST_REWARD_TIME, 0L) ?: 0L

    fun setLastRewardTimestamp(timestamp: Long) {
        sharedPrefs?.edit()?.putLong(KEY_LAST_REWARD_TIME, timestamp)?.apply()
    }

    fun getLanguage(): String = sharedPrefs?.getString(KEY_LANGUAGE, "TH") ?: "TH"

    fun setLanguage(lang: String) {
        sharedPrefs?.edit()?.putString(KEY_LANGUAGE, lang)?.apply()
    }

    fun isSlipUsed(txId: String): Boolean {
        val history = sharedPrefs?.getString("used_slips_history", "") ?: ""
        return history.contains(txId)
    }

    fun saveUsedSlip(txId: String) {
        val history = sharedPrefs?.getString("used_slips_history", "") ?: ""
        sharedPrefs?.edit()?.putString("used_slips_history", "$history|$txId")?.apply()
    }
}
