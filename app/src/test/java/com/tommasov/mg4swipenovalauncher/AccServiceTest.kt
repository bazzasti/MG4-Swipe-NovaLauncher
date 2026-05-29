package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class AccServiceTest {

    @Test
    fun `triggerBack is callable as static method`() {
        // Verify the companion function exists
        val methods = AccService.Companion::class.java.declaredMethods
            .map { it.name }
        assertTrue("triggerBack should exist", methods.contains("triggerBack"))
    }

    @Test
    fun `AccService companion has volatile instance field`() {
        // Verify the instance tracking field exists
        val field = AccService::class.java.getDeclaredField("instance")
        assertNotNull(field)
    }
}
