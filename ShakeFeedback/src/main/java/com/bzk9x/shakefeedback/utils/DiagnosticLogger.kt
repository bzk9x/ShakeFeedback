package com.bzk9x.shakefeedback.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for gathering diagnostic logs and system information.
 * Used to provide context when shake feedback is triggered.
 */
object DiagnosticLogger {
    /**
     * Collect diagnostic information about the device and app state.
     */
    fun collectDiagnostics(context: Context): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        sb.append("=== SHAKE FEEDBACK DIAGNOSTIC LOG ===\n")
        sb.append("Timestamp: $timestamp\n")
        sb.append("\n")

        // Device Information
        sb.append("--- Device Information ---\n")
        sb.append("Device: ${android.os.Build.DEVICE}\n")
        sb.append("Model: ${android.os.Build.MODEL}\n")
        sb.append("Manufacturer: ${android.os.Build.MANUFACTURER}\n")
        sb.append("Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n")
        sb.append("\n")

        // Memory Information
        sb.append("--- Memory Information ---\n")
        try {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory() / 1024 / 1024
            val freeMemory = runtime.freeMemory() / 1024 / 1024
            val maxMemory = runtime.maxMemory() / 1024 / 1024

            sb.append("Total Memory: ${totalMemory}MB\n")
            sb.append("Free Memory: ${freeMemory}MB\n")
            sb.append("Max Memory: ${maxMemory}MB\n")
        } catch (e: Exception) {
            sb.append("Memory info unavailable: ${e.message}\n")
        }
        sb.append("\n")

        // App Process Information
        sb.append("--- App Process Information ---\n")
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)

            sb.append("System Available Memory: ${memInfo.availMem / 1024 / 1024}MB\n")
            sb.append("Low Memory: ${memInfo.lowMemory}\n")
        } catch (e: Exception) {
            sb.append("Process info unavailable: ${e.message}\n")
        }
        sb.append("\n")

        // Storage Information
        sb.append("--- Storage Information ---\n")
        try {
            val externalState = Environment.getExternalStorageState()
            sb.append("External Storage State: $externalState\n")
            if (externalState == Environment.MEDIA_MOUNTED) {
                val externalDir = Environment.getExternalStorageDirectory()
                val totalSpace = externalDir.totalSpace / 1024 / 1024
                val freeSpace = externalDir.freeSpace / 1024 / 1024
                sb.append("Total Space: ${totalSpace}MB\n")
                sb.append("Free Space: ${freeSpace}MB\n")
            }
        } catch (e: Exception) {
            sb.append("Storage info unavailable: ${e.message}\n")
        }
        sb.append("\n")

        // Logcat Tail (last 50 lines)
        sb.append("--- Recent Logs (Last 50 lines) ---\n")
        try {
            val process = Runtime.getRuntime().exec("logcat -d -t 50")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
        } catch (e: Exception) {
            sb.append("Logs unavailable: ${e.message}\n")
        }

        return sb.toString()
    }
}
