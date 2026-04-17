package com.example.discord_alert_system.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ファイルおよびLogcatにDiscord通知の内容を記録する。
 *
 * ログファイル: <filesDir>/discord_notifications.log
 * フォーマット: [YYYY-MM-DD HH:mm:ss] sender="..." text="..."
 *
 * ファイルが 1 MB を超えると自動でローテート（古い方は .bak に退避）。
 */
object NotificationLogger {

    private const val TAG = "DiscordNotif"
    private const val LOG_FILE = "discord_notifications.log"
    private const val LOG_FILE_BAK = "discord_notifications.log.bak"
    private const val MAX_SIZE_BYTES = 1_048_576L // 1 MB

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun log(context: Context, sender: String, text: String) {
        val timestamp = dateFormat.format(Date())
        val line = "[$timestamp] sender=\"$sender\" text=\"$text\""

        // Logcat
        Log.d(TAG, line)

        // ファイル書き込み（IOは呼び出し元のバックグラウンドスレッドで実行される）
        try {
            val file = File(context.filesDir, LOG_FILE)
            rotate(file, context)
            file.appendText(line + "\n")
        } catch (e: Exception) {
            Log.e(TAG, "ログファイルへの書き込みに失敗しました", e)
        }
    }

    /** ログファイルのパスを返す（UI表示やシェア用）。 */
    fun logFile(context: Context): File = File(context.filesDir, LOG_FILE)

    private fun rotate(file: File, context: Context) {
        if (file.exists() && file.length() > MAX_SIZE_BYTES) {
            val bak = File(context.filesDir, LOG_FILE_BAK)
            bak.delete()
            file.renameTo(bak)
        }
    }
}
