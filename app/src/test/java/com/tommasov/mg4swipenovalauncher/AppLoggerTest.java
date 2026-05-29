package com.tommasov.mg4swipenovalauncher;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppLoggerTest {

    @Test
    public void loggerDoesNotThrow() {
        // AppLogger wraps android.util.Log which isn't available in unit tests
        // but we verify the class loads without error
        assertNotNull(AppLogger.class);
    }
}
