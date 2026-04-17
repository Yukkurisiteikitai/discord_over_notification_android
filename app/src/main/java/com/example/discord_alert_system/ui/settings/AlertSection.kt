package com.example.discord_alert_system.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong

@Composable
fun AlertSection(
    strobeIntervalMs: Long,
    alertDurationMs: Long,
    onStrobeIntervalChange: (Long) -> Unit,
    onAlertDurationChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Alert Settings", style = MaterialTheme.typography.titleSmall)

        // Strobe speed: 100 ms – 500 ms half-period
        LabeledSlider(
            label = "Strobe interval: ${strobeIntervalMs} ms",
            value = strobeIntervalMs.toFloat(),
            valueRange = 100f..500f,
            onValueChange = { onStrobeIntervalChange(it.roundToLong()) },
        )

        // Alert duration: 5 s – 120 s
        LabeledSlider(
            label = "Alert duration: ${alertDurationMs / 1000} s",
            value = alertDurationMs.toFloat(),
            valueRange = 5_000f..120_000f,
            onValueChange = { onAlertDurationChange(it.roundToLong()) },
        )

        Text(
            text = "⚠\uFE0F Photosensitivity warning: strobe frequencies above 3 Hz (< 333 ms) " +
                "may trigger seizures in susceptible individuals. Default 250 ms = 2 Hz.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
