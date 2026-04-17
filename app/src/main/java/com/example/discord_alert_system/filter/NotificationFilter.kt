package com.example.discord_alert_system.filter

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.example.discord_alert_system.data.SettingsState

/**
 * Pure Kotlin filter — no Android framework state, fully unit-testable.
 *
 * Discord notification formats:
 *   DM:     title = "SenderName",        text = "message body"
 *   Server: title = "#channel-name",     text = "SenderName: message body"
 *   Group:  title = "GroupName",         text = "SenderName: message body"
 *
 * Both title AND the sender prefix extracted from text are checked against
 * the configured senders list, so DMs and server/group messages both match.
 */
object NotificationFilter {

    private const val DISCORD_PACKAGE = "com.discord"

    fun matches(sbn: StatusBarNotification, settings: SettingsState): Boolean {
        if (sbn.packageName != DISCORD_PACKAGE) return false

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text  = extras.getString(Notification.EXTRA_TEXT)  ?: ""

        // For server/group messages the sender is the prefix before the first ":"
        val senderInText = if (text.contains(":")) text.substringBefore(":").trim() else ""

        val senderMatch = settings.senders.isEmpty() || settings.senders.any { configured ->
            title.contains(configured, ignoreCase = true) ||
                senderInText.contains(configured, ignoreCase = true)
        }

        val keywordMatch = settings.keywords.isEmpty() || settings.keywords.any { kw ->
            title.contains(kw, ignoreCase = true) || text.contains(kw, ignoreCase = true)
        }

        return senderMatch && keywordMatch
    }
}
