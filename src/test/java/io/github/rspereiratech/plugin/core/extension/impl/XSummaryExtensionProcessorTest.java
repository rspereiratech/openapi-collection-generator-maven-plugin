package io.github.rspereiratech.plugin.core.extension.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.extension.ExtensionContext;
import io.github.rspereiratech.plugin.core.extension.ExtensionResult;

import io.swagger.v3.oas.models.Operation;

class XSummaryExtensionProcessorTest {

    private final XSummaryExtensionProcessor p = new XSummaryExtensionProcessor();

    @Test
    void supports_onlyMatchesXSummary() {
        assertTrue(p.supports("x-summary"));
        assertFalse(p.supports("x-other"));
    }

    @Test
    void process_overridesName_whenStringValue() {
        ExtensionResult r = p.process("x-summary", "Custom Name",
                new ExtensionContext("/p", "GET", "Orig", "", new Operation()));
        assertEquals("Custom Name", r.nameOverride());
        assertNull(r.descriptionAppend());
    }

    @Test
    void process_returnsNoChange_whenBlank() {
        ExtensionResult r = p.process("x-summary", "   ",
                new ExtensionContext("/p", "GET", "Orig", "", new Operation()));
        assertNull(r.nameOverride());
    }

    @Test
    void process_returnsNoChange_whenNotString() {
        ExtensionResult r = p.process("x-summary", 42,
                new ExtensionContext("/p", "GET", "Orig", "", new Operation()));
        assertNull(r.nameOverride());
    }

    @Test
    void process_returnsNoChange_whenNull() {
        ExtensionResult r = p.process("x-summary", null,
                new ExtensionContext("/p", "GET", "Orig", "", new Operation()));
        assertNull(r.nameOverride());
    }
}
