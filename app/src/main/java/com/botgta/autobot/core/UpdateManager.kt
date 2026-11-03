package com.botgta.autobot.core

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    private const val TAG = "UpdateManager"
    
    // 🔴 แก้ไขชื่อผู้ใช้ GitHub และคลังเก็บไฟล์ของคุณที่นี่
    private const val GITHUB_OWNER = "sahaphumx-lab"
    private const val GITHUB_REPO = "MCF-Updates" // ชื่อ Repo ที่ตั้งเป็น Public ไว้เก็บ APK

    private var currentDownloadId: Long = -1L
    var latestVersion: String = "..." // เวอร์ชันล่าสุดจาก GitHub

    suspend fun checkForUpdate(
        activity: Activity, 
        onUpdateFound: (version: String, changelog: String, url: String) -> Unit
    ) {
        val currentVersion = packageVersionName(activity)
        Log.d(TAG, "Current Version: $currentVersion")

        withContext(Dispatchers.IO) {
            try {
                // เช็คอัปเดตผ่าน GitHub API แบบฟรี
                val url = URL("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    
                    val tagName = json.getString("tag_name").replace("v", "") // เช่น v1.0.2 -> 1.0.2
                    latestVersion = "v$tagName"
                    val body = json.getString("body") // รายละเอียดอัปเดต
                    val assets = json.getJSONArray("assets")

                    var downloadUrl = ""
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }

                    if (downloadUrl.isNotEmpty() && isNewerVersion(currentVersion, tagName)) {
                        Log.d(TAG, "🚀 New version found! Latest: $tagName, Current: $currentVersion")
                        withContext(Dispatchers.Main) {
                            onUpdateFound(tagName, body, downloadUrl)
                        }
                    } else {
                        Log.d(TAG, "✅ App is up to date. (Current: $currentVersion, Latest: $tagName)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed: ${e.message}")
            }
            Unit
        }
    }

    // ลบชุดเก่าออก เพราะเราจะไปวาดใน Compose แทน

    fun startDownload(context: Context, url: String, fileName: String, onProgress: (Float) -> Unit = {}) {
        if (url.isEmpty()) {
            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, "❌ ลิงก์ดาวน์โหลดไม่ถูกต้อง", Toast.LENGTH_SHORT).show()
            }
            return
        }

        Toast.makeText(context, "📥 เริ่มดาวน์โหลดเวอร์ชันใหม่...", Toast.LENGTH_SHORT).show()
        val appContext = context.applicationContext

        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "❌ ดาวน์โหลดล้มเหลว: เซิร์ฟเวอร์ตอบกลับ ${connection.responseCode}", Toast.LENGTH_LONG).show()
                        onProgress(0f)
                    }
                    return@launch
                }

                val fileLength = connection.contentLength
                val downloadFile = File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                
                if (downloadFile.exists()) {
                    downloadFile.delete()
                }

                val input = connection.inputStream
                val output = java.io.FileOutputStream(downloadFile)
                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                var lastProgress = 0f

                withContext(Dispatchers.Main) { onProgress(0.01f) }

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)
                    if (fileLength > 0) {
                        val progress = (total.toFloat() / fileLength.toFloat()).coerceIn(0.01f, 1f)
                        if (progress - lastProgress > 0.01f || progress == 1f) {
                            lastProgress = progress
                            withContext(Dispatchers.Main) { onProgress(progress) }
                        }
                    }
                }
                output.flush()
                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    onProgress(1f)
                    installApk(appContext, fileName)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "❌ เกิดข้อผิดพลาดในการดาวน์โหลด: ${e.message}", Toast.LENGTH_LONG).show()
                    onProgress(0f)
                }
            }
        }
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        Log.d(TAG, "Installing APK from: ${file.absolutePath}")
        
        if (!file.exists()) {
            Log.e(TAG, "APK File not found at ${file.absolutePath}")
            Toast.makeText(context, "❌ ไม่พบไฟล์ติดตั้ง โปรดลองดาวน์โหลดใหม่", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")

            Log.d(TAG, "Starting install activity with URI: $apkUri")
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install failed: ${e.message}")
            Toast.makeText(context, "❌ การติดตั้งล้มเหลว: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun packageVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // Logic ง่ายๆ ในการเปรียบเทียบเวอร์ชัน (1.0.1 > 1.0.0)
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currParts = current.trim().split(".").map { it.toIntOrNull() ?: 0 }
        val latParts = latest.trim().split(".").map { it.toIntOrNull() ?: 0 }
        
        Log.d(TAG, "Comparing versions: Current=$currParts, Latest=$latParts")
        
        for (i in 0 until Math.max(currParts.size, latParts.size)) {
            val c = currParts.getOrElse(i) { 0 }
            val l = latParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
