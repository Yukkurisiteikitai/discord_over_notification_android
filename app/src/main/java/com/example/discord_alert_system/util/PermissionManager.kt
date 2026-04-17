package com.example.discord_alert_system.util

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.discord_alert_system.service.DiscordNotificationListenerService

object PermissionManager {

    /** True when the user has granted notification listener access. */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        val component = ComponentName(context, DiscordNotificationListenerService::class.java)
        return flat.contains(component.flattenToString())
    }

    /** True when Do-Not-Disturb policy access is granted (needed to override DnD). */
    fun isDndPolicyGranted(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java)
            .isNotificationPolicyAccessGranted

    /** Returns an Intent that opens the Notification Access settings screen. */
    fun notificationAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    /** Returns an Intent that opens the Do-Not-Disturb settings screen. */
    fun dndSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
}
