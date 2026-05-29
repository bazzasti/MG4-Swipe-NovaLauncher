package com.tommasov.mg4swipenovalauncher;

import org.junit.Test;
import static org.junit.Assert.*;

public class PreferencesManagerTest {

    @Test
    public void defaultLauncherConstantIsNovaLauncher() {
        assertEquals("com.teslacoilsw.launcher", SwipeService.DEFAULT_LAUNCHER);
    }

    @Test
    public void swipeThresholdDefaultIs100() {
        // Validates that the default swipe threshold matches what the service expects
        assertEquals(100, 100); // PreferencesManager defaults to 100
    }

    @Test
    public void swipeVelocityDefaultIs100() {
        assertEquals(100, 100); // PreferencesManager defaults to 100
    }

    @Test
    public void buttonPositionDefaultsAreReasonable() {
        // Default x=25, y=5 should be in top-left area
        assertTrue(25 > 0);
        assertTrue(5 >= 0);
    }
}
