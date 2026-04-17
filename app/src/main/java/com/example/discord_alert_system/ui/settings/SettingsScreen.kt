package com.example.discord_alert_system.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            FilterSection(
                label = "Keywords",
                items = settings.keywords,
                onAdd = viewModel::addKeyword,
                onRemove = viewModel::removeKeyword,
                modifier = Modifier.padding(vertical = 12.dp),
            )

            HorizontalDivider()

            FilterSection(
                label = "Senders",
                items = settings.senders,
                onAdd = viewModel::addSender,
                onRemove = viewModel::removeSender,
                modifier = Modifier.padding(vertical = 12.dp),
            )

            HorizontalDivider()

            QuietHoursSection(
                enabled = settings.quietHoursEnabled,
                startTime = settings.quietHoursStart,
                endTime = settings.quietHoursEnd,
                isCurrentlyQuiet = settings.isQuietHoursActive(),
                onUpdate = viewModel::updateQuietHours,
                modifier = Modifier.padding(vertical = 12.dp),
            )

            HorizontalDivider()

            AlertSection(
                strobeIntervalMs = settings.strobeIntervalMs,
                alertDurationMs = settings.alertDurationMs,
                onStrobeIntervalChange = viewModel::updateStrobeInterval,
                onAlertDurationChange = viewModel::updateAlertDuration,
                modifier = Modifier.padding(vertical = 12.dp),
            )

            HorizontalDivider()

            PermissionsSection(modifier = Modifier.padding(vertical = 12.dp))
        }
    }
}
