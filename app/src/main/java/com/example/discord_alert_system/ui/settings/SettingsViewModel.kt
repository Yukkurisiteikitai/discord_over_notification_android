package com.example.discord_alert_system.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.discord_alert_system.data.SettingsRepository
import com.example.discord_alert_system.data.SettingsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SettingsRepository(app)

    val settings: StateFlow<SettingsState> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun addKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val current = settings.value.keywords
            if (trimmed !in current) repository.updateKeywords(current + trimmed)
        }
    }

    fun removeKeyword(keyword: String) {
        viewModelScope.launch {
            repository.updateKeywords(settings.value.keywords - keyword)
        }
    }

    fun addSender(sender: String) {
        val trimmed = sender.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val current = settings.value.senders
            if (trimmed !in current) repository.updateSenders(current + trimmed)
        }
    }

    fun removeSender(sender: String) {
        viewModelScope.launch {
            repository.updateSenders(settings.value.senders - sender)
        }
    }

    fun updateStrobeInterval(intervalMs: Long) {
        viewModelScope.launch { repository.updateStrobeInterval(intervalMs) }
    }

    fun updateAlertDuration(durationMs: Long) {
        viewModelScope.launch { repository.updateAlertDuration(durationMs) }
    }

    fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        viewModelScope.launch { repository.updateQuietHours(enabled, start, end) }
    }
}
