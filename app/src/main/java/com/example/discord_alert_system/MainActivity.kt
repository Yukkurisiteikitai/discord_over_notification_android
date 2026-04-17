package com.example.discord_alert_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.discord_alert_system.ui.MainNavigation
import com.example.discord_alert_system.ui.theme.Discord_alert_systemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Discord_alert_systemTheme {
                MainNavigation()
            }
        }
    }
}
