package com.aware.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTest {
    @Test
    fun `oled key restores oled appearance`() {
        assertEquals(Appearance.OLED, Appearance.fromKey("oled"))
    }

    @Test
    fun `unknown appearance falls back to system`() {
        assertEquals(Appearance.SYSTEM, Appearance.fromKey("unknown"))
    }
}
