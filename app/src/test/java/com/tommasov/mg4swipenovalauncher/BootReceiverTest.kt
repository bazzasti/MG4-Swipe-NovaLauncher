package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class BootReceiverTest {

    @Test
    fun `BootReceiver has onReceive method`() {
        val methods = BootReceiver::class.java.declaredMethods.map { it.name }
        assertTrue("Should have onReceive", methods.contains("onReceive"))
    }

    @Test
    fun `BootReceiver is instantiable`() {
        assertNotNull(BootReceiver())
    }
}
