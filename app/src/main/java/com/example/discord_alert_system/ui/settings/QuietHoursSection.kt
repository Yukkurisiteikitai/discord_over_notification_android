package com.example.discord_alert_system.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun QuietHoursSection(
    enabled: Boolean,
    startTime: String,
    endTime: String,
    isCurrentlyQuiet: Boolean,
    onUpdate: (Boolean, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var enabledInput by remember(enabled) { mutableStateOf(enabled) }
    var startInput by remember(startTime) { mutableStateOf(startTime) }
    var endInput by remember(endTime) { mutableStateOf(endTime) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        Text("Quiet hours", style = MaterialTheme.typography.titleSmall)

        Text(
            text = "Set a time range where alerts will not turn on.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = enabledInput,
                onCheckedChange = { enabledInput = it },
            )
            Text("Enable quiet hours")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = startInput,
                onValueChange = { startInput = it },
                label = { Text("Start (HH:mm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = endInput,
                onValueChange = { endInput = it },
                label = { Text("End (HH:mm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Button(
                onClick = {
                    val validationError = validateTimeRange(startInput, endInput)
                    if (validationError == null) {
                        errorMessage = null
                        onUpdate(enabledInput, startInput.trim(), endInput.trim())
                    } else {
                        errorMessage = validationError
                    }
                },
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isCurrentlyQuiet) "Now: suppressed" else "Now: active",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrentlyQuiet) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private fun validateTimeRange(start: String, end: String): String? {
    if (start.toLocalTimeOrNull() == null) return "Start time must be HH:mm"
    if (end.toLocalTimeOrNull() == null) return "End time must be HH:mm"
    return null
}

private fun String.toLocalTimeOrNull(): LocalTime? = try {
    LocalTime.parse(trim(), TIME_FORMATTER)
} catch (_: DateTimeParseException) {
    null
}

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")