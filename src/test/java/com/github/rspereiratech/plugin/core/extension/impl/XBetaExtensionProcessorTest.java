package com.github.rspereiratech.plugin.core.extension.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.extension.ExtensionContext;
import com.github.rspereiratech.plugin.core.extension.ExtensionResult;

import io.swagger.v3.oas.models.Operation;

class XBetaExtensionProcessorTest {

    private final XBetaExtensionProcessor p = new XBetaExtensionProcessor();

    @Test
    void supports_onlyMatchesXBeta() {
        assertTrue(p.supports("x-beta"));
        assertFalse(p.supports("x-other"));
        assertFalse(p.supports(""));
    }

    @Test
    void process_appendsBetaToName_whenTrue() {
        ExtensionResult r = p.process("x-beta", true,
                new ExtensionContext("/p", "GET", "MyOp", "", new Operation()));
        assertEquals("MyOp (Beta)", r.nameOverride());
        assertTrue(r.descriptionAppend().contains("Beta"));
    }

    @Test
    void process_returnsNoChange_whenNotTrue() {
        ExtensionResult r = p.process("x-beta", false,
                new ExtensionContext("/p", "GET", "n", "", new Operation()));
        assertNull(r.nameOverride());
        assertNull(r.descriptionAppend());
    }

    @Test
    void process_returnsNoChange_whenNull() {
        ExtensionResult r = p.process("x-beta", null,
                new ExtensionContext("/p", "GET", "n", "", new Operation()));
        assertNull(r.nameOverride());
    }
}
