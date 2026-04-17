package com.example.discord_alert_system.data

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class SettingsState(
    val keywords: List<String> = emptyList(),
    val senders: List<String> = emptyList(),
    // Strobe half-period: 250 ms = 2 Hz (photosensitivity-safe default).
    // Configurable range: 100–500 ms.
    val strobeIntervalMs: Long = 250L,
    val alertDurationMs: Long = 30_000L,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    // recastMs == alertDurationMs: same sender+keyword combo is silenced
    // for the entire alert window to prevent re-triggering.
) {
    val recastMs: Long get() = alertDurationMs

    fun isQuietHoursActive(now: LocalTime = LocalTime.now()): Boolean {
        if (!quietHoursEnabled) return false

        val start = quietHoursStart.toLocalTimeOrNull() ?: return false
        val end = quietHoursEnd.toLocalTimeOrNull() ?: return false

        return when {
            start == end -> false
            start.isBefore(end) -> !now.isBefore(start) && now.isBefore(end)
            else -> !now.isBefore(start) || now.isBefore(end)
        }
    }

    private fun String.toLocalTimeOrNull(): LocalTime? = try {
        LocalTime.parse(this, TIME_FORMATTER)
    } catch (_: DateTimeParseException) {
        null
    }

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
