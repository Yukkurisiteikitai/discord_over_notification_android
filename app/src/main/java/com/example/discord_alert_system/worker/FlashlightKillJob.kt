package com.example.discord_alert_system.worker

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager safety net: turns off the flashlight even if AlertForegroundService
 * is killed by the OS before it can call setTorchMode(false) in its finally block.
 *
 * Scheduled with REPLACE policy at alert start with delay = alertDurationMs + 5 000 ms.
 * Cancelled on normal service stop so it only fires if the service dies unexpectedly.
 */
class FlashlightKillJob(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val cameraManager = applicationContext.getSystemService(CameraManager::class.java)
        cameraManager.cameraIdList
            .filter { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            .forEach { id ->
                runCatching { cameraManager.setTorchMode(id, false) }
            }
        return Result.success()
    }
}
