package io.github.rspereiratech.plugin.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UUIDGeneratorTest {

    private final UUIDGenerator gen = new UUIDGenerator();

    @Test
    void generate_appliesPrefixWithUnderscore() {
        String id = gen.generate("wrk", "anything");
        assertTrue(id.startsWith("wrk_"));
    }

    @Test
    void generate_stripsHyphensFromUUID() {
        String id = gen.generate("p", "ctx");
        assertFalse(id.substring(2).contains("-"));
        assertEquals(2 + 32, id.length());
    }

    @Test
    void generate_isUnique_perCall() {
        assertNotEquals(gen.generate("p", "c"), gen.generate("p", "c"));
    }
}
