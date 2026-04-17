package com.example.discord_alert_system.alert

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** アラートの稼働状態をプロセス内で共有するシングルトン。 */
object AlertState {
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    internal fun setActive(active: Boolean) {
        _isActive.value = active
    }
}
