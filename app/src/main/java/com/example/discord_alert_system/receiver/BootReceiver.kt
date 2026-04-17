package com.example.discord_alert_system.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives BOOT_COMPLETED so the app can prompt the user to re-enable
 * notification access after a reboot (Android automatically re-connects
 * bound NotificationListenerService components, but this receiver lets us
 * ensure WorkManager is initialised and the user is notified if access
 * was revoked during the reboot cycle).
 *
 * Note: NotificationListenerService itself is reconnected by the OS on boot;
 * this receiver is a hook for any additional initialisation work.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // WorkManager auto-reschedules persisted work after reboot — no explicit
        // action needed here. Add explicit startup tasks below if required.
    }
}
