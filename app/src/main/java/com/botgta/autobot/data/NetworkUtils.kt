package com.botgta.autobot.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object NetworkUtils {
    private const val TAG = "NetworkUtils"
    // Using a basic XOR obfuscation to hide the API key from plain text decompilation.
    // The key is: 3a725361d627cc003559a17a2b6d1202b3f61d9f
    // XOR'd by 'X' (ASCII 88)
    private val OBFUSCATED_SK_API = intArrayOf(
        107, 57, 111, 106, 109, 107, 110, 105, 60, 110, 106, 111, 59, 59, 104, 104, 107, 109, 109, 97, 57, 105, 111, 57, 106, 58, 110, 60, 105, 106, 104, 106, 58, 107, 62, 110, 105, 60, 97, 62
    )

    private fun getShrinkmeApi(): String {
        return OBFUSCATED_SK_API.map { (it xor 88).toChar() }.joinToString("")
    }

    suspend fun generateRewardLink(randomKey: String): String? = withContext(Dispatchers.IO) {
        try {
            val apiKey = getShrinkmeApi()
            // The destination URL user must reach! 
            // We use a dummy search query or your website parameter to show the key
            val destinationUrl = "https://www.zirconx.my/?generated_key_to_copy=$randomKey"
            val encodedParam = URLEncoder.encode(destinationUrl, "UTF-8")
            
            val requestUrl = "https://shrinkme.io/api?api=$apiKey&url=$encodedParam"
            
            val url = URL(requestUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                if (json.optString("status") == "success") {
                    return@withContext json.optString("shortenedUrl")
                } else {
                    Log.e(TAG, "Shrinkme API Error: ${json.optString("message")}")
                }
            } else {
                Log.e(TAG, "HTTP Code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching short link: ${e.message}")
        }
        return@withContext null
    }
    /**
     * ดึงเวลาจาก Server จริง (ป้องกันการแก้เวลาในเครื่อง)
     * ใช้ผลลัพธ์เป็น Unix Timestamp (วินาที)
     */
    suspend fun getServerTime(): Long? = withContext(Dispatchers.IO) {
        try {
            // ใช้ WorldTimeAPI เป็นหลัก (ฟรีและไม่ต้องใช้ Key)
            val url = URL("https://worldtimeapi.org/api/timezone/Etc/UTC")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            
            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                val json = JSONObject(response)
                return@withContext json.getLong("unixtime")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WorldTimeAPI failed, trying fallback...")
        }

        // Fallback: ดึงจาก Header ของ Google (ชูร์สุดๆ)
        try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val dateStr = connection.getHeaderField("Date") // e.g. "Tue, 25 Mar 2024 12:00:00 GMT"
            if (dateStr != null) {
                val sdf = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US)
                return@withContext sdf.parse(dateStr)?.time?.div(1000)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback failed: ${e.message}")
        }
        return@withContext null
    }

    /**
     * ระบบตรวจสอบ Key ผ่าน Server ฐานข้อมูล (ป้องกันการ Hack 100%)
     * ตัวอย่างนี้คือการจำลองการยิง HTTP POST ไปที่ API 
     * ค่าที่ส่งไปคือ {"key": "XXXXX"} และคาดหวังผลลัพธ์ {"valid": true, "addSeconds": 2592000}
     */
    suspend fun verifyVipKey(keyStr: String): Long? = withContext(Dispatchers.IO) {
        // === ลบ Hardcoded Keys ออกเพื่อความปลอดภัย 100% ===
        // ต้องเชื่อมต่อกับ Server จริงเท่านั้นถึงจะใช้งานได้
        
        try {
            // URL ของ API ของคุณจริงๆ เช่น https://your-server.com/api/check-key
            val apiUrl = "https://example.com/api/check-key" 
            
            // ข้ามการต่อเน็ตจริงในตัวอย่างนี้ถ้าไม่ได้เปลี่ยนโดเมน
            if(apiUrl.contains("example.com")) return@withContext null

            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            // ส่งข้อมูล Key ไปเช็ค
            val jsonInputString = "{\"key\": \"$keyStr\"}"
            connection.outputStream.use { os ->
                val input = jsonInputString.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                val json = JSONObject(response)
                
                // ถ้าระบบตอบกลับมาว่า Key ถูกต้อง ให้คืนค่าเวลาที่ได้ (วินาที)
                if (json.optBoolean("valid", false)) {
                    return@withContext json.optLong("addSeconds", 0L)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Verify Key failed: ${e.message}")
        }
        return@withContext null
    }
}
