package com.example.discord_alert_system.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.discord_alert_system.ui.components.PermissionStatusCard
import com.example.discord_alert_system.util.PermissionManager

@Composable
fun PermissionsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Re-check on each recomposition (user may have returned from Settings)
    var listenerGranted by remember { mutableStateOf(PermissionManager.isNotificationListenerEnabled(context)) }
    var dndGranted by remember { mutableStateOf(PermissionManager.isDndPolicyGranted(context)) }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        listenerGranted = PermissionManager.isNotificationListenerEnabled(context)
        dndGranted = PermissionManager.isDndPolicyGranted(context)
    }

    Column(modifier = modifier) {
        Text("Permissions", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp))

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
    }
}
