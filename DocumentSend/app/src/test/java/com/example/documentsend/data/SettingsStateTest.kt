package com.example.documentsend.data

import org.junit.Assert.*
import org.junit.Test

class SettingsStateTest {

    @Test
    fun `default values should be correct`() {
        val state = SettingsState()

        assertEquals("默认用户", state.userName)
        assertEquals(0, state.themeMode)
        assertFalse(state.autoSave)
        assertEquals("默认", state.colorScheme)
        assertTrue(state.saveToHistory)
    }

    @Test
    fun `should create with custom values`() {
        val state = SettingsState(
            userName = "自定义用户",
            themeMode = 2,
            autoSave = true,
            colorScheme = "红色",
            saveToHistory = false
        )

        assertEquals("自定义用户", state.userName)
        assertEquals(2, state.themeMode)
        assertTrue(state.autoSave)
        assertEquals("红色", state.colorScheme)
        assertFalse(state.saveToHistory)
    }

    @Test
    fun `copy should create new instance`() {
        val original = SettingsState(userName = "old")
        val copied = original.copy(userName = "new")

        assertEquals("new", copied.userName)
        assertNotEquals(original, copied)
    }

    @Test
    fun `copy should preserve other fields`() {
        val original = SettingsState(
            userName = "user",
            themeMode = 1,
            autoSave = true,
            colorScheme = "绿色"
        )
        val copied = original.copy(userName = "changed")

        assertEquals(1, copied.themeMode)
        assertTrue(copied.autoSave)
        assertEquals("绿色", copied.colorScheme)
    }

    @Test
    fun `equals should work correctly`() {
        val s1 = SettingsState(userName = "test")
        val s2 = SettingsState(userName = "test")
        val s3 = SettingsState(userName = "other")

        assertEquals(s1, s2)
        assertNotEquals(s1, s3)
    }

    @Test
    fun `hashCode should be equal for equal objects`() {
        val s1 = SettingsState(userName = "test")
        val s2 = SettingsState(userName = "test")

        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun `themeMode 0 should represent follow system`() {
        val state = SettingsState(themeMode = 0)
        assertEquals(0, state.themeMode)
    }

    @Test
    fun `themeMode 1 should represent light`() {
        val state = SettingsState(themeMode = 1)
        assertEquals(1, state.themeMode)
    }

    @Test
    fun `themeMode 2 should represent dark`() {
        val state = SettingsState(themeMode = 2)
        assertEquals(2, state.themeMode)
    }
}
