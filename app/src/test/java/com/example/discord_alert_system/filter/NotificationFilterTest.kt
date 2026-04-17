package com.example.discord_alert_system.filter

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.example.discord_alert_system.data.SettingsState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for NotificationFilter.
 *
 * Covers:
 *   - DM notification (title = sender name)
 *   - Server/group notification (text = "SenderName: message")
 *   - Empty keyword/sender filters (pass-all)
 *   - Non-Discord package (always rejected)
 *   - Cooldown: tested in AlertOrchestratorTest (not here — needs clock)
 */
class NotificationFilterTest {

    private fun makeSbn(
        packageName: String = "com.discord",
        title: String = "",
        text: String = "",
    ): StatusBarNotification {
        val extras = Bundle().apply {
            putString(Notification.EXTRA_TITLE, title)
            putString(Notification.EXTRA_TEXT, text)
        }
        val notification = mock<Notification>()
        notification.extras = extras

        val sbn = mock<StatusBarNotification>()
        whenever(sbn.packageName).thenReturn(packageName)
        whenever(sbn.notification).thenReturn(notification)
        return sbn
    }

    // ── Non-Discord package ──────────────────────────────────────────────────

    @Test
    fun `rejects non-discord package even if filters empty`() {
        val sbn = makeSbn(packageName = "com.slack")
        assertFalse(NotificationFilter.matches(sbn, SettingsState()))
    }

    // ── Empty filters = pass-all ─────────────────────────────────────────────

    @Test
    fun `empty filters accept any discord notification`() {
        val sbn = makeSbn(title = "SomeUser", text = "hello")
        assertTrue(NotificationFilter.matches(sbn, SettingsState()))
    }

    // ── DM format (title = sender) ───────────────────────────────────────────

    @Test
    fun `DM matches when sender filter equals title`() {
        val sbn = makeSbn(title = "Alice", text = "hey there")
        val settings = SettingsState(senders = listOf("Alice"))
        assertTrue(NotificationFilter.matches(sbn, settings))
    }

    @Test
    fun `DM sender match is case-insensitive`() {
        val sbn = makeSbn(title = "ALICE", text = "hey")
        val settings = SettingsState(senders = listOf("alice"))
        assertTrue(NotificationFilter.matches(sbn, settings))
    }

    @Test
    fun `DM rejects when sender does not match`() {
        val sbn = makeSbn(title = "Bob", text = "hey")
        val settings = SettingsState(senders = listOf("Alice"))
        assertFalse(NotificationFilter.matches(sbn, settings))
    }

    // ── Server/group format (text = "SenderName: message") ──────────────────

    @Test
    fun `server message matches sender extracted from text prefix`() {
        val sbn = makeSbn(title = "#general", text = "Alice: hello everyone")
        val settings = SettingsState(senders = listOf("Alice"))
        assertTrue(NotificationFilter.matches(sbn, settings))
    }

    @Test
    fun `server message rejects when sender prefix does not match`() {
        val sbn = makeSbn(title = "#general", text = "Bob: hello everyone")
        val settings = SettingsState(senders = listOf("Alice"))
        assertFalse(NotificationFilter.matches(sbn, settings))
    }

    // ── Keyword filter ───────────────────────────────────────────────────────

    @Test
    fun `keyword match in text body`() {
        val sbn = makeSbn(title = "Alice", text = "the server is on fire urgent")
        val settings = SettingsState(keywords = listOf("urgent"))
        assertTrue(NotificationFilter.matches(sbn, settings))
    }

    @Test
    fun `keyword match in title`() {
        val sbn = makeSbn(title = "URGENT alert", text = "something happened")
        val settings = SettingsState(keywords = listOf("urgent"))
        assertTrue(NotificationFilter.matches(sbn, settings))
    }

    @Test
    fun `keyword not present → rejected`() {
        val sbn = makeSbn(title = "Alice", text = "just a normal message")
        val settings = SettingsState(keywords = listOf("urgent"))
        assertFalse(NotificationFilter.matches(sbn, settings))
    }

    // ── Combined sender + keyword ────────────────────────────────────────────

    @Test
    fun `both sender and keyword must match`() {
        val sbn = makeSbn(title = "#ops", text = "Alice: urgent deploy needed")
        val settings = SettingsState(senders = listOf("Alice"), keywords = listOf("urgent"))
        assertTrue(NotificationFilter.matches(sbn, settings))
    }

    @Test
    fun `sender matches but keyword missing → rejected`() {
        val sbn = makeSbn(title = "#ops", text = "Alice: everything is fine")
        val settings = SettingsState(senders = listOf("Alice"), keywords = listOf("urgent"))
        assertFalse(NotificationFilter.matches(sbn, settings))
    }
}
