package io.github.rspereiratech.plugin.core.extension.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.extension.ExtensionContext;
import io.github.rspereiratech.plugin.core.extension.ExtensionResult;

import io.swagger.v3.oas.models.Operation;

class XInternalExtensionProcessorTest {

    private final XInternalExtensionProcessor p = new XInternalExtensionProcessor();

    @Test
    void supports_onlyMatchesXInternal() {
        assertTrue(p.supports("x-internal"));
        assertFalse(p.supports("x-beta"));
    }

    @Test
    void process_appendsInternalToName() {
        ExtensionResult r = p.process("x-internal", true,
                new ExtensionContext("/p", "GET", "Op", "", new Operation()));
        assertEquals("Op (Internal)", r.nameOverride());
        assertTrue(r.descriptionAppend().contains("Internal"));
    }

    @Test
    void process_returnsNoChange_whenFalse() {
        ExtensionResult r = p.process("x-internal", false,
                new ExtensionContext("/p", "GET", "Op", "", new Operation()));
        assertNull(r.nameOverride());
        assertNull(r.descriptionAppend());
    }
}
