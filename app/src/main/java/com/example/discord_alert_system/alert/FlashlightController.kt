package com.example.discord_alert_system.alert

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.delay

/**
 * Strobes the device flashlight for [durationMs] milliseconds using
 * CameraManager.setTorchMode() — no CAMERA permission required (API 23+).
 *
 * The half-period is coerced to at least 100 ms to satisfy Android CTS floor
 * and avoid hardware damage on back-to-back toggle calls.
 */
class FlashlightController(private val cameraManager: CameraManager) {

    private fun flashCameraId(): String? =
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

    suspend fun strobe(intervalMs: Long, durationMs: Long) {
        val cameraId = flashCameraId() ?: return   // device has no flash
        val halfPeriod = intervalMs.coerceAtLeast(100)
        val end = System.currentTimeMillis() + durationMs
        try {
            while (System.currentTimeMillis() < end) {
                cameraManager.setTorchMode(cameraId, true)
                delay(halfPeriod)
                cameraManager.setTorchMode(cameraId, false)
                delay(halfPeriod)
            }
        } finally {
            // Guaranteed off — even if coroutine is cancelled mid-cycle
            runCatching { cameraManager.setTorchMode(cameraId, false) }
        }
    }
}
