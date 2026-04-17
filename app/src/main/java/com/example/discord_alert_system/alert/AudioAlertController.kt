package com.example.discord_alert_system.alert

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.discord_alert_system.R

/**
 * Overrides ringer mode and plays alarm.mp3 at maximum alarm stream volume.
 *
 * If Do-Not-Disturb policy access is not granted the alarm AudioAttributes
 * (USAGE_ALARM / CONTENT_TYPE_SONIFICATION) bypass DnD in most configurations
 * without requiring ACCESS_NOTIFICATION_POLICY.
 *
 * Always restores the previous ringer mode and volume when [stop] is called.
 */
class AudioAlertController(private val context: Context) {

    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private var mediaPlayer: MediaPlayer? = null
    private var savedRingerMode: Int = AudioManager.RINGER_MODE_NORMAL
    private var savedAlarmVolume: Int = 0

    fun play() {
        savedRingerMode = audioManager.ringerMode
        savedAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        // Override ringer + set alarm volume to max
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0,
        )

        // Bypass DnD if policy access is granted; alarm stream handles it otherwise
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }

        mediaPlayer = MediaPlayer().apply {
            val afd = context.resources.openRawResourceFd(R.raw.alarm)
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            setAudioStreamType(AudioManager.STREAM_ALARM)
            isLooping = true
            prepare()
            start()
        }
    }

    fun stop() {
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        // Restore saved state
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, savedAlarmVolume, 0)
            audioManager.ringerMode = savedRingerMode
        }
    }
}
