package io.github.rspereiratech.plugin.insomnia.deprecated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InsomniaDeprecationMarkerTest {

    private final InsomniaDeprecationMarker marker = new InsomniaDeprecationMarker();

    @Test
    void markName_returnsOriginal_whenNotDeprecated() {
        assertEquals("name", marker.markName("name", false));
    }

    @Test
    void markName_addsWarningPrefix_andDeprecatedSuffix_whenDeprecated() {
        String r = marker.markName("name", true);
        assertTrue(r.contains("name"));
        assertTrue(r.contains("deprecated"));
    }

    @Test
    void markDescription_returnsOriginal_whenNotDeprecated() {
        assertEquals("desc", marker.markDescription("desc", false));
    }

    @Test
    void markDescription_prependsWarning_whenDeprecated() {
        String r = marker.markDescription("desc", true);
        assertTrue(r.startsWith("DEPRECATED"));
        assertTrue(r.contains("desc"));
    }

    @Test
    void markDescription_blankInput_returnsJustWarning() {
        String r = marker.markDescription("", true);
        assertTrue(r.startsWith("DEPRECATED"));
    }
}
