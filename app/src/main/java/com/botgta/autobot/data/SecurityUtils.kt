package com.botgta.autobot.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import java.util.Locale
import java.io.File

object SecurityUtils {
    // Encrypted SHA-256 Signature (XOR with key 0x42)
    private val ENC_SIG = intArrayOf(
        3,114,114,1,122,113,6,3,3,112,0,117,1,7,6,4,0,113,4,6,3,3,6,119,113,115,115,117,114,1,4,112,119,119,1,116,0,116,117,123,117,116,113,7,123,113,119,113,6,119,118,119,7,7,119,122,4,114,123,7,117,3,119,114
    )

    private fun getValidSignature(): String {
        return ENC_SIG.map { (it xor 0x42).toChar() }.joinToString("")
    }

    // รหัสเครื่องที่ได้รับอนุญาต (Admin Device IDs) - Obfuscated
    private val ADMIN_ID_PARTS = listOf("ef", "0c", "e37c", "0aba", "6c30")
    private val ADMIN_ID_PARTS_2 = listOf("2b", "06", "864", "415b", "1488") // Backup Admin ID

    fun getDeviceID(context: Context): String {
        return android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    }

    fun isDeviceAuthorized(context: Context): Boolean {
        val currentID = getDeviceID(context).lowercase().trim()
        val fullID1 = ADMIN_ID_PARTS.joinToString("").lowercase().trim()
        val fullID2 = ADMIN_ID_PARTS_2.joinToString("").lowercase().trim()
        return currentID == fullID1 || currentID == fullID2
    }

    @Suppress("DEPRECATION")
    fun isSignatureValid(context: Context): Boolean {
        try {
            val packageName = context.packageName
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                if (packageInfo.signingInfo?.hasMultipleSigners() == true) {
                    packageInfo.signingInfo.apkContentsSigners
                } else {
                    packageInfo.signingInfo?.signingCertificateHistory ?: emptyArray()
                }
            } else {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                packageInfo.signatures ?: emptyArray()
            }

            val validSignature = getValidSignature()
            for (signature in signatures) {
                val sha256 = getSHA256(signature.toByteArray())
                if (sha256 == validSignature) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun getSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02X".format(it) }
    }

    fun isDebuggerConnected(): Boolean {
        return android.os.Debug.isDebuggerConnected() || android.os.Debug.waitingForDebugger()
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase(Locale.getDefault()).contains("vbox")
                || Build.FINGERPRINT.lowercase(Locale.getDefault()).contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }
}
