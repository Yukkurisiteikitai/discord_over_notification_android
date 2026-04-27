package com.example.discord_alert_system.ui.settings

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.discord_alert_system.R
import com.example.discord_alert_system.data.SettingsRepository
import com.example.discord_alert_system.data.SettingsState
import com.example.discord_alert_system.data.SoundOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = SettingsRepository(app)

    val settings: StateFlow<SettingsState> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    private var testPlayer: MediaPlayer? = null
    private val _isTestPlaying = MutableStateFlow(false)
    val isTestPlaying: StateFlow<Boolean> = _isTestPlaying

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

    fun addUploadedSound(uriString: String) {
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            val name = app.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: uri.lastPathSegment ?: "Sound"
            repository.addUploadedSound(SoundOption(uriString, name))
            repository.selectSound(uriString)
        }
    }

    fun removeUploadedSound(uri: String) {
        viewModelScope.launch { repository.removeUploadedSound(uri) }
    }

    fun selectSound(uri: String?) {
        viewModelScope.launch { repository.selectSound(uri) }
    }

    fun toggleTestSound() {
        if (_isTestPlaying.value) stopTestSound() else startTestSound()
    }

    private fun startTestSound() {
        val app = getApplication<Application>()
        val uri = settings.value.selectedSoundUri
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val player = MediaPlayer().apply {
                    if (uri != null) {
                        setDataSource(app, Uri.parse(uri))
                    } else {
                        val afd = app.resources.openRawResourceFd(R.raw.alarm)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                    }
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = false
                    setOnCompletionListener { stopTestSound() }
                    prepare()
                    start()
                }
                testPlayer = player
                _isTestPlaying.value = true
            }.onFailure {
                _isTestPlaying.value = false
            }
        }
    }

    fun stopTestSound() {
        testPlayer?.runCatching { if (isPlaying) stop(); release() }
        testPlayer = null
        _isTestPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopTestSound()
    }
}
