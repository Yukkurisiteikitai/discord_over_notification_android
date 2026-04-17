package com.example.discord_alert_system.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_KEYWORDS = stringSetPreferencesKey("keywords")
        private val KEY_SENDERS = stringSetPreferencesKey("senders")
        private val KEY_STROBE_INTERVAL = longPreferencesKey("strobe_interval_ms")
        private val KEY_ALERT_DURATION = longPreferencesKey("alert_duration_ms")
        private val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        private val KEY_QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        private val KEY_QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
    }

    val settingsFlow: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        SettingsState(
            keywords = prefs[KEY_KEYWORDS]?.toList() ?: emptyList(),
            senders = prefs[KEY_SENDERS]?.toList() ?: emptyList(),
            strobeIntervalMs = prefs[KEY_STROBE_INTERVAL] ?: 250L,
            alertDurationMs = prefs[KEY_ALERT_DURATION] ?: 30_000L,
            quietHoursEnabled = prefs[KEY_QUIET_HOURS_ENABLED] ?: false,
            quietHoursStart = prefs[KEY_QUIET_HOURS_START] ?: "22:00",
            quietHoursEnd = prefs[KEY_QUIET_HOURS_END] ?: "07:00",
        )
    }

    suspend fun updateKeywords(keywords: List<String>) {
        context.dataStore.edit { it[KEY_KEYWORDS] = keywords.toSet() }
    }

    suspend fun updateSenders(senders: List<String>) {
        context.dataStore.edit { it[KEY_SENDERS] = senders.toSet() }
    }

    suspend fun updateStrobeInterval(intervalMs: Long) {
        context.dataStore.edit { it[KEY_STROBE_INTERVAL] = intervalMs }
    }

    suspend fun updateAlertDuration(durationMs: Long) {
        context.dataStore.edit { it[KEY_ALERT_DURATION] = durationMs }
    }

    suspend fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        context.dataStore.edit {
            it[KEY_QUIET_HOURS_ENABLED] = enabled
            it[KEY_QUIET_HOURS_START] = start
            it[KEY_QUIET_HOURS_END] = end
        }
    }
}
