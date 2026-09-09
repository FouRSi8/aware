package com.aware.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class CozyPaletteTest {
    @Test
    fun missingPreferenceUsesOriginalPalette() {
        assertEquals(CozyPalette.OAT_GARDEN, CozyPalette.fromKey(null))
        assertEquals(CozyPalette.OAT_GARDEN, CozyPalette.fromKey("unknown"))
    }

    @Test
    fun everyPersistedKeyRoundTrips() {
        CozyPalette.entries.forEach { palette ->
            assertEquals(palette, CozyPalette.fromKey(palette.key))
        }
    }
}
