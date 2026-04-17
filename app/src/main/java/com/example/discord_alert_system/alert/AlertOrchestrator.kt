package com.example.discord_alert_system.alert

import android.content.Context
import android.content.Intent
import com.example.discord_alert_system.data.SettingsState
import com.example.discord_alert_system.service.AlertForegroundService
import java.util.concurrent.ConcurrentHashMap

/**
 * Cooldown gate that prevents the same (sender + keyword) pair from triggering
 * more than one alert per [SettingsState.recastMs] window.
 *
 * Thread-safe: [trigger] may be called from the notification listener thread.
 */
object AlertOrchestrator {

    private val lastAlertTime = ConcurrentHashMap<String, Long>()

    /**
     * @param sender  The sender name extracted from the notification.
     * @param keyword The keyword that matched (empty string if no keyword filter is set).
     */
    fun trigger(
        settings: SettingsState,
        sender: String,
        keyword: String,
        context: Context,
    ) {
        val key = "$sender|$keyword"
        val now = System.currentTimeMillis()
        val last = lastAlertTime[key] ?: 0L

        if (now - last < settings.recastMs) return   // still inside cooldown window
        if (settings.isQuietHoursActive()) return

        lastAlertTime[key] = now

        val intent = Intent(context, AlertForegroundService::class.java).apply {
            putExtra(AlertForegroundService.EXTRA_DURATION, settings.alertDurationMs)
            putExtra(AlertForegroundService.EXTRA_STROBE_INTERVAL, settings.strobeIntervalMs)
        }
        context.startForegroundService(intent)
    }
}
