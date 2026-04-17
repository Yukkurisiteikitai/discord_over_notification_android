package com.example.discord_alert_system.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.discord_alert_system.alert.AlertOrchestrator
import com.example.discord_alert_system.data.SettingsRepository
import com.example.discord_alert_system.data.SettingsState
import com.example.discord_alert_system.filter.NotificationFilter
import com.example.discord_alert_system.util.NotificationLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives all posted notifications and forwards matching Discord alerts to
 * [AlertOrchestrator]. The service is system-bound and not subject to background
 * execution restrictions.
 */
class DiscordNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        repository = SettingsRepository(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Discord以外のパッケージは無視
        if (sbn.packageName != "com.discord") return

        scope.launch {
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text  = extras.getString(Notification.EXTRA_TEXT)  ?: ""

            // 送信者の判定:
            //   DM        → title がそのままユーザー名
            //   サーバー/グループ → text が "ユーザー名: メッセージ" 形式
            val sender = if (text.contains(":")) text.substringBefore(":").trim() else title
            val body   = if (text.contains(":")) text.substringAfter(":").trim() else text

            NotificationLogger.log(applicationContext, sender, body)

            val settings = repository.settingsFlow.first()
            if (!NotificationFilter.matches(sbn, settings)) return@launch

            val (matchedSender, keyword) = extractSenderAndKeyword(sbn, settings)
            AlertOrchestrator.trigger(settings, matchedSender, keyword, applicationContext)
        }
    }

    /**
     * Returns the best matching sender + keyword pair for cooldown keying.
     * Uses the first matching keyword (or empty if no keyword filter configured).
     */
    private fun extractSenderAndKeyword(
        sbn: StatusBarNotification,
        settings: SettingsState,
    ): Pair<String, String> {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text  = extras.getString(Notification.EXTRA_TEXT)  ?: ""
        val senderInText = if (text.contains(":")) text.substringBefore(":").trim() else ""

        val sender = when {
            settings.senders.isEmpty() -> title
            settings.senders.any { title.contains(it, ignoreCase = true) } -> title
            else -> senderInText
        }

        val keyword = settings.keywords.firstOrNull { kw ->
            title.contains(kw, ignoreCase = true) || text.contains(kw, ignoreCase = true)
        } ?: ""

        return sender to keyword
    }
}
