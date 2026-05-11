package io.github.rspereiratech.plugin.postman.deprecated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PostmanDeprecationMarkerTest {

    private final PostmanDeprecationMarker m = new PostmanDeprecationMarker();

    @Test
    void markName_unchanged_whenNotDeprecated() {
        assertEquals("n", m.markName("n", false));
    }

    @Test
    void markName_prefixes_DEPRECATED() {
        assertEquals("[DEPRECATED] n", m.markName("n", true));
    }

    @Test
    void markDescription_unchanged_whenNotDeprecated() {
        assertEquals("d", m.markDescription("d", false));
    }

    @Test
    void markDescription_prependsWarning() {
        String r = m.markDescription("d", true);
        assertTrue(r.contains("deprecated"));
        assertTrue(r.contains("d"));
    }

    @Test
    void markDescription_blankInput_warningOnly() {
        String r = m.markDescription("", true);
        assertTrue(r.contains("deprecated"));
    }
}
