package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class SwipeServiceTest {

    // ── Constants ────────────────────────────────────────────────────────────

    @Test
    fun `DEFAULT_LAUNCHER is Nova Launcher package`() {
        assertEquals("com.teslacoilsw.launcher", SwipeService.DEFAULT_LAUNCHER)
    }

    @Test
    fun `default swipe threshold is 100`() {
        assertEquals(100, SwipeService.DEFAULT_SWIPE_THRESHOLD)
    }

    @Test
    fun `default swipe velocity is 100`() {
        assertEquals(100, SwipeService.DEFAULT_SWIPE_VELOCITY)
    }

    @Test
    fun `default zone height is 12dp`() {
        assertEquals(12, SwipeService.DEFAULT_ZONE_HEIGHT_DP)
    }

    @Test
    fun `default button position is top-left corner`() {
        assertTrue(SwipeService.DEFAULT_BUTTON_X > 0)
        assertTrue(SwipeService.DEFAULT_BUTTON_Y >= 0)
    }

    // ── isSwipeUp logic ──────────────────────────────────────────────────────

    @Test
    fun `swipe up detected when finger moves up fast enough`() {
        // startY=500, endY=300 → diffY=-200 (moved up 200px)
        // velocityY=-500 (moving upward at 500px/s)
        assertTrue(SwipeService.isSwipeUp(500f, 300f, -500f, 100, 100))
    }

    @Test
    fun `swipe down is not detected as swipe up`() {
        // startY=300, endY=500 → diffY=+200 (moved DOWN)
        assertFalse(SwipeService.isSwipeUp(300f, 500f, 500f, 100, 100))
    }

    @Test
    fun `tiny upward movement is not a swipe`() {
        // Only moved 30px up — below 100px threshold
        assertFalse(SwipeService.isSwipeUp(500f, 470f, -500f, 100, 100))
    }

    @Test
    fun `slow upward movement is not a swipe`() {
        // Moved 200px up but velocity only 50px/s — below 100px/s threshold
        assertFalse(SwipeService.isSwipeUp(500f, 300f, -50f, 100, 100))
    }

    @Test
    fun `exact threshold values are not a swipe (must exceed)`() {
        // diffY=-100 exactly, velocity=100 exactly — must be GREATER than
        assertFalse(SwipeService.isSwipeUp(500f, 400f, -100f, 100, 100))
    }

    @Test
    fun `just above threshold is detected`() {
        // diffY=-101, velocity=-101
        assertTrue(SwipeService.isSwipeUp(500f, 399f, -101f, 100, 100))
    }

    @Test
    fun `horizontal movement is not a swipe up`() {
        // No vertical movement at all
        assertFalse(SwipeService.isSwipeUp(500f, 500f, 0f, 100, 100))
    }

    @Test
    fun `negative velocity with downward movement is not a swipe`() {
        // Finger went down but velocity is negative (contradictory — edge case)
        assertFalse(SwipeService.isSwipeUp(300f, 500f, -500f, 100, 100))
    }

    @Test
    fun `custom threshold works correctly`() {
        // With threshold=50, a 60px movement should be detected
        assertTrue(SwipeService.isSwipeUp(500f, 440f, -200f, 50, 50))
        // But 40px should not
        assertFalse(SwipeService.isSwipeUp(500f, 460f, -200f, 50, 50))
    }

    @Test
    fun `custom velocity threshold works correctly`() {
        // With velocity threshold=200, velocity=-150 should not trigger
        assertFalse(SwipeService.isSwipeUp(500f, 300f, -150f, 100, 200))
        // But velocity=-250 should
        assertTrue(SwipeService.isSwipeUp(500f, 300f, -250f, 100, 200))
    }

    @Test
    fun `very fast short swipe is not detected`() {
        // Fast velocity but too short distance
        assertFalse(SwipeService.isSwipeUp(500f, 480f, -1000f, 100, 100))
    }

    @Test
    fun `very long slow swipe is not detected`() {
        // Long distance but too slow
        assertFalse(SwipeService.isSwipeUp(500f, 100f, -30f, 100, 100))
    }

    @Test
    fun `zero thresholds detect any upward movement`() {
        // With threshold=0, even 1px up at any speed should detect
        assertTrue(SwipeService.isSwipeUp(500f, 499f, -1f, 0, 0))
    }

    @Test
    fun `positive velocityY with upward movement still detects`() {
        // velocityY sign is positive but finger moved up — abs(velocityY) > threshold
        assertTrue(SwipeService.isSwipeUp(500f, 300f, 500f, 100, 100))
    }
}
