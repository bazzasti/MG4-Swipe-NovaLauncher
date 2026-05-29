package com.tommasov.mg4swipenovalauncher

import org.junit.Assert.*
import org.junit.Test

class AppLoggerTest {

    @Test
    fun `logger is an object singleton`() {
        assertNotNull(AppLogger)
    }

    @Test
    fun `logger has all four log methods`() {
        val methods = AppLogger::class.java.methods.map { it.name }
        assertTrue("Should have i(String)", methods.contains("i"))
        assertTrue("Should have w(String)", methods.contains("w"))
        assertTrue("Should have e(String)", methods.contains("e"))
    }

    @Test
    fun `logger has error method with throwable parameter`() {
        val eMethods = AppLogger::class.java.methods.filter { it.name == "e" }
        val hasThrowableOverload = eMethods.any { m ->
            m.parameterTypes.size == 2 && m.parameterTypes[1] == Throwable::class.java
        }
        assertTrue("Should have e(String, Throwable) overload", hasThrowableOverload)
    }
}
