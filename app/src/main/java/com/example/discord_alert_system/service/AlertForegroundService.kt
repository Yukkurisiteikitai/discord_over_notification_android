package com.example.discord_alert_system.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.discord_alert_system.R
import com.example.discord_alert_system.alert.AlertState
import com.example.discord_alert_system.alert.AudioAlertController
import com.example.discord_alert_system.alert.FlashlightController
import com.example.discord_alert_system.worker.FlashlightKillJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * アラートを実行するフォアグラウンドサービス。
 *
 * 停止方法は3つ：
 *   1. alertDurationMs 経過後の自動停止
 *   2. ACTION_STOP インテントの受信（アプリ内ボタン / 通知アクションボタン）
 *   3. WorkManager の FlashlightKillJob ウォッチドッグ（プロセスキル後の安全網）
 */
class AlertForegroundService : Service() {

    companion object {
        const val EXTRA_DURATION = "duration"
        const val EXTRA_STROBE_INTERVAL = "strobe_interval"
        const val ACTION_STOP = "com.example.discord_alert_system.action.STOP_ALERT"
        private const val CHANNEL_ID = "discord_alert_channel"
        private const val NOTIFICATION_ID = 1001
        internal const val WATCHDOG_WORK_NAME = "torch_kill_watchdog"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var flashlight: FlashlightController
    private lateinit var audio: AudioAlertController
    private var alertJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        flashlight = FlashlightController(getSystemService(CameraManager::class.java))
        audio = AudioAlertController(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 通知ボタン or アプリ内ボタンからの停止リクエスト
        if (intent?.action == ACTION_STOP) {
            if (alertJob == null) {
                stopAlertImmediately()
            } else {
                alertJob?.cancel()   // finally ブロックがクリーンアップを担う
            }
            return START_NOT_STICKY
        }

        val durationMs = intent?.getLongExtra(EXTRA_DURATION, 30_000L) ?: 30_000L
        val strobeIntervalMs = intent?.getLongExtra(EXTRA_STROBE_INTERVAL, 250L) ?: 250L

        startForeground(NOTIFICATION_ID, buildNotification())
        AlertState.setActive(true)
        scheduleWatchdog(durationMs)

        alertJob = scope.launch {
            try {
                val strobeJob = launch { flashlight.strobe(strobeIntervalMs, durationMs) }
                audio.play()
                strobeJob.join()
            } finally {
                // キャンセル・正常終了どちらでも必ず通る
                stopAlertImmediately()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun stopPendingIntent(): PendingIntent {
        val stopIntent = Intent(this, AlertForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun scheduleWatchdog(alertDurationMs: Long) {
        val request = OneTimeWorkRequestBuilder<FlashlightKillJob>()
            .setInitialDelay(alertDurationMs + 5_000L, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(WATCHDOG_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private fun cancelWatchdog() {
        WorkManager.getInstance(applicationContext)
            .cancelUniqueWork(WATCHDOG_WORK_NAME)
    }

    private fun stopAlertImmediately() {
        audio.stop()
        cancelWatchdog()
        AlertState.setActive(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Discord Alert",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "アラート稼働中に表示されます"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Discord Alert 稼働中")
            .setContentText("フラッシュ・音声アラートが実行中です")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            // 通知上の停止ボタン
            .addAction(
                android.R.drawable.ic_media_pause,
                "停止",
                stopPendingIntent(),
            )
            .build()
}
