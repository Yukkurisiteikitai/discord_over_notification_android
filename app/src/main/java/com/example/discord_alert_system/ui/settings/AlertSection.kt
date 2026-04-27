package com.example.discord_alert_system.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.discord_alert_system.data.SoundOption
import kotlin.math.roundToLong

@Composable
fun AlertSection(
    strobeIntervalMs: Long,
    alertDurationMs: Long,
    uploadedSounds: List<SoundOption>,
    selectedSoundUri: String?,
    isTestPlaying: Boolean,
    onStrobeIntervalChange: (Long) -> Unit,
    onAlertDurationChange: (Long) -> Unit,
    onSoundAdded: (String) -> Unit,
    onSoundRemoved: (String) -> Unit,
    onSoundSelected: (String?) -> Unit,
    onTestSoundToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onSoundAdded(uri.toString())
        }
    }

    Column(modifier = modifier) {
        Text("Alert Settings", style = MaterialTheme.typography.titleSmall)

        // Sound selection
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Alert Sound", style = MaterialTheme.typography.bodyMedium)

            // Horizontal toggle chips: [Default] [Sound1 ×] [Sound2 ×] ... [+ Add]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = selectedSoundUri == null,
                    onClick = { onSoundSelected(null) },
                    label = { Text("Default") },
                )

                uploadedSounds.forEach { sound ->
                    FilterChip(
                        selected = selectedSoundUri == sound.uri,
                        onClick = { onSoundSelected(sound.uri) },
                        label = { Text(sound.name) },
                    )
                }

                OutlinedButton(
                    onClick = { soundPickerLauncher.launch(arrayOf("audio/*")) },
                ) {
                    Text("+ Add")
                }
            }

            // Remove button — only shown when an uploaded sound is selected
            if (selectedSoundUri != null) {
                val selectedName = uploadedSounds.find { it.uri == selectedSoundUri }?.name ?: "sound"
                OutlinedButton(
                    onClick = { onSoundRemoved(selectedSoundUri) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text("Remove \"$selectedName\"")
                }
            }

            // Test sound button
            OutlinedButton(
                onClick = onTestSoundToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(if (isTestPlaying) "■ Stop Test" else "▶ Test Sound")
            }
        }

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
            text = "⚠️ Photosensitivity warning: strobe frequencies above 3 Hz (< 333 ms) " +
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
