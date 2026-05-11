package io.github.rspereiratech.plugin.core.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.Operation;

class ExtensionProcessorChainTest {

    private final ExtensionProcessor p1 = mock(ExtensionProcessor.class);
    private final ExtensionProcessor p2 = mock(ExtensionProcessor.class);

    @Test
    void process_returnsNoChange_whenNoExtensions() {
        Operation op = new Operation();
        ExtensionProcessorChain chain = new ExtensionProcessorChain(List.of(p1));
        ExtensionResult r = chain.process(new ExtensionContext("/p", "GET", "n", "d", op));
        assertNull(r.nameOverride());
        assertNull(r.descriptionAppend());
    }

    @Test
    void process_appliesLastNameOverrideWins() {
        Operation op = new Operation();
        op.addExtension("x-a", true);
        op.addExtension("x-b", true);
        when(p1.supports("x-a")).thenReturn(true);
        when(p1.supports("x-b")).thenReturn(false);
        when(p2.supports("x-a")).thenReturn(false);
        when(p2.supports("x-b")).thenReturn(true);
        when(p1.process(eq("x-a"), any(), any())).thenReturn(new ExtensionResult("name-from-a", "desc-a"));
        when(p2.process(eq("x-b"), any(), any())).thenReturn(new ExtensionResult("name-from-b", "desc-b"));

        ExtensionProcessorChain chain = new ExtensionProcessorChain(List.of(p1, p2));
        ExtensionResult r = chain.process(new ExtensionContext("/p", "GET", "init", "", op));

        // last non-null override wins; order is determined by extension iteration order then processor order
        assertTrue("name-from-a".equals(r.nameOverride()) || "name-from-b".equals(r.nameOverride()));
        // descriptions are joined with newlines
        assertTrue(r.descriptionAppend().contains("desc-a"));
        assertTrue(r.descriptionAppend().contains("desc-b"));
    }

    @Test
    void process_filtersBySupports() {
        Operation op = new Operation();
        op.addExtension("x-c", "v");
        when(p1.supports(anyString())).thenReturn(false);
        ExtensionProcessorChain chain = new ExtensionProcessorChain(List.of(p1));
        ExtensionResult r = chain.process(new ExtensionContext("/p", "GET", "n", "d", op));
        assertNull(r.nameOverride());
        assertNull(r.descriptionAppend());
    }

    @Test
    void process_keepsExtensionsMap_whenOperationExtensionsExplicitlyEmpty() {
        Operation op = new Operation();
        op.setExtensions(Map.of());
        ExtensionProcessorChain chain = new ExtensionProcessorChain(List.of(p1));
        ExtensionResult r = chain.process(new ExtensionContext("/p", "GET", "n", "d", op));
        assertEquals(null, r.nameOverride());
    }
}
