package io.github.rspereiratech.plugin.core.extension.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.extension.ExtensionContext;
import io.github.rspereiratech.plugin.core.extension.ExtensionResult;

import io.swagger.v3.oas.models.Operation;

class XDeprecatedSinceExtensionProcessorTest {

    private final XDeprecatedSinceExtensionProcessor p = new XDeprecatedSinceExtensionProcessor();

    @Test
    void supports_onlyMatchesKey() {
        assertTrue(p.supports("x-deprecated-since"));
        assertFalse(p.supports("x-beta"));
    }

    @Test
    void process_appendsVersionToDescription() {
        ExtensionResult r = p.process("x-deprecated-since", "1.2.0",
                new ExtensionContext("/p", "GET", "n", "", new Operation()));
        assertNull(r.nameOverride());
        assertTrue(r.descriptionAppend().contains("1.2.0"));
        assertTrue(r.descriptionAppend().contains("Deprecated since"));
    }

    @Test
    void process_returnsNoChange_whenValueNull() {
        ExtensionResult r = p.process("x-deprecated-since", null,
                new ExtensionContext("/p", "GET", "n", "", new Operation()));
        assertNull(r.nameOverride());
        assertNull(r.descriptionAppend());
    }

    @Test
    void process_appendsNonStringValueAsToString() {
        ExtensionResult r = p.process("x-deprecated-since", 42,
                new ExtensionContext("/p", "GET", "n", "", new Operation()));
        assertEquals("Deprecated since: **42**", r.descriptionAppend());
    }
}
