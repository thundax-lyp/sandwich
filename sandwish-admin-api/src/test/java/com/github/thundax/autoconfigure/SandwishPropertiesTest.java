package com.github.thundax.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.junit.Test;

public class SandwishPropertiesTest {

    @Test
    public void shouldUseWritableDefaultSysLogStoragePath() {
        SandwishProperties.LogProperties logProperties = new SandwishProperties.LogProperties();

        String storagePath = logProperties.getStoragePath();

        assertTrue(storagePath.startsWith(System.getProperty("java.io.tmpdir")));
        assertTrue(storagePath.endsWith("sandwish" + File.separator + "sys-log" + File.separator));
    }

    @Test
    public void shouldNormalizeConfiguredSysLogStoragePath() {
        SandwishProperties.LogProperties logProperties = new SandwishProperties.LogProperties();
        logProperties.setStoragePath("storage/sys-log");

        assertEquals("storage/sys-log" + File.separator, logProperties.getStoragePath());
    }
}
