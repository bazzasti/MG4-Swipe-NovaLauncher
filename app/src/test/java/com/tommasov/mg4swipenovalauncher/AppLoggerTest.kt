package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class AppLoggerTest {

    @Test
    fun `logger class loads without error`() {
        assertNotNull(AppLogger::class.java)
    }
}
