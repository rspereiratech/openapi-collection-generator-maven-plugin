package io.github.rspereiratech.plugin.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeterministicIdGeneratorTest {

    private final DeterministicIdGenerator gen = new DeterministicIdGenerator();

    @Test
    void generate_isDeterministic_forSameInput() {
        String a = gen.generate("req", "GET /pets");
        String b = gen.generate("req", "GET /pets");
        assertEquals(a, b);
    }

    @Test
    void generate_appliesPrefix() {
        String id = gen.generate("wrk", "foo");
        assertTrue(id.startsWith("wrk_"));
    }

    @Test
    void generate_truncatesHashTo16Chars() {
        String id = gen.generate("p", "ctx");
        // prefix "p_" + 16 hex chars
        assertEquals(2 + 16, id.length());
    }

    @Test
    void generate_differentContexts_produceDifferentIds() {
        assertNotEquals(gen.generate("p", "a"), gen.generate("p", "b"));
    }
}
