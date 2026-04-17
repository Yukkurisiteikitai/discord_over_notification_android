package com.example.discord_alert_system.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.discord_alert_system.alert.AlertState
import com.example.discord_alert_system.service.AlertForegroundService
import com.example.discord_alert_system.ui.components.PermissionStatusCard
import com.example.discord_alert_system.util.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val isAlertActive by AlertState.isActive.collectAsState()

    var listenerGranted by remember { mutableStateOf(PermissionManager.isNotificationListenerEnabled(context)) }
    var dndGranted by remember { mutableStateOf(PermissionManager.isDndPolicyGranted(context)) }
    var postNotificationsGranted by remember { mutableStateOf(true) }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        listenerGranted = PermissionManager.isNotificationListenerEnabled(context)
        dndGranted = PermissionManager.isDndPolicyGranted(context)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        postNotificationsGranted = granted
    }

    // Request POST_NOTIFICATIONS on first composition (API 33+)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Discord Alert") }) },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text(
                "System Status",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            PermissionStatusCard(
                title = "Notification Access",
                granted = listenerGranted,
                actionLabel = "Open Settings",
                onAction = { settingsLauncher.launch(PermissionManager.notificationAccessSettingsIntent()) },
            )

            PermissionStatusCard(
                title = "Do Not Disturb Override",
                granted = dndGranted,
                actionLabel = "Open Settings",
                onAction = { settingsLauncher.launch(PermissionManager.dndSettingsIntent()) },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionStatusCard(
                    title = "Post Notifications",
                    granted = postNotificationsGranted,
                    actionLabel = "Grant",
                    onAction = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }

            if (listenerGranted) {
                Text(
                    "Monitoring Discord notifications…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (isAlertActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Alert is running",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Button(
                    onClick = {
                        context.startService(
                            Intent(context, AlertForegroundService::class.java).apply {
                                action = AlertForegroundService.ACTION_STOP
                            }
                        )
                    },
                ) {
                    Text("停止")
                }
            }
        }
    }
}
