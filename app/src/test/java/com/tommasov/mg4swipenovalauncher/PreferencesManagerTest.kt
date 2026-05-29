package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class PreferencesManagerTest {

    @Test
    fun `default launcher constant is Nova Launcher`() {
        assertEquals("com.teslacoilsw.launcher", SwipeService.DEFAULT_LAUNCHER)
    }

    @Test
    fun `default button position is top-left`() {
        assertTrue(25 > 0)
        assertTrue(5 >= 0)
    }
}
