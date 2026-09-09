package com.aware.app.update

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUpdaterTest {
    @Test fun comparesSemanticVersionsNumerically() {
        assertEquals(1, AppUpdater.compareVersions("1.10.0", "1.9.9"))
        assertEquals(-1, AppUpdater.compareVersions("1.2.7", "2.0.0"))
        assertEquals(0, AppUpdater.compareVersions("1.2.7", "1.2.7"))
        assertEquals(0, AppUpdater.compareVersions("1.2.7-beta", "1.2.7"))
    }
}
