package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class PreferencesManagerTest {

    @Test
    fun `default launcher constant matches SwipeService`() {
        assertEquals("com.teslacoilsw.launcher", SwipeService.DEFAULT_LAUNCHER)
    }

    @Test
    fun `default button position matches SwipeService constants`() {
        assertEquals(25, SwipeService.DEFAULT_BUTTON_X)
        assertEquals(5, SwipeService.DEFAULT_BUTTON_Y)
    }

    @Test
    fun `default swipe thresholds are reasonable`() {
        // 100px threshold and 100px/s velocity are sensible defaults
        val threshold = SwipeService.DEFAULT_SWIPE_THRESHOLD
        val velocity = SwipeService.DEFAULT_SWIPE_VELOCITY
        assertTrue("Threshold should be > 0", threshold > 0)
        assertTrue("Threshold should be < 500", threshold < 500)
        assertTrue("Velocity should be > 0", velocity > 0)
        assertTrue("Velocity should be < 1000", velocity < 1000)
    }

    @Test
    fun `PreferencesManager is a singleton class`() {
        // Verify getInstance exists as a companion method
        val method = PreferencesManager.Companion::class.java.methods
            .find { it.name == "getInstance" }
        assertNotNull("getInstance should exist", method)
    }

    @Test
    fun `zone height is reasonable for touch targets`() {
        // Android accessibility guidelines say 48dp minimum for touch targets
        // but swipe zones are intentionally small (12dp) since they're invisible
        val height = SwipeService.DEFAULT_ZONE_HEIGHT_DP
        assertTrue("Zone should be > 0", height > 0)
        assertTrue("Zone should be <= 48dp (intentionally small)", height <= 48)
    }
}
