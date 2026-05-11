package com.github.rspereiratech.plugin.core.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;

class DefaultCallbackProcessorTest {

    private final DefaultCallbackProcessor processor = new DefaultCallbackProcessor();

    @Test
    void extractCallbackPaths_returnsEmpty_whenCallbacksNull() {
        Map<String, PathItem> result = processor.extractCallbackPaths(null, "op", new OpenAPI());
        assertTrue(result.isEmpty());
    }

    @Test
    void extractCallbackPaths_returnsEmpty_whenCallbacksEmpty() {
        Map<String, PathItem> result = processor.extractCallbackPaths(Map.of(), "op", new OpenAPI());
        assertTrue(result.isEmpty());
    }

    @Test
    void extractCallbackPaths_replacesRuntimeExpressionWithSyntheticPath() {
        Operation cbOp = new Operation();
        PathItem pi = new PathItem().post(cbOp);
        Callback cb = new Callback();
        cb.addPathItem("{$request.body#/callbackUrl}", pi);
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        callbacks.put("myEvent", cb);

        Map<String, PathItem> result = processor.extractCallbackPaths(callbacks, "createPet", new OpenAPI());

        assertEquals(1, result.size());
        String key = result.keySet().iterator().next();
        assertEquals("/callbacks/createPet/myEvent", key);
        assertEquals("myEvent", cbOp.getSummary());
        assertNotNull(cbOp.getDescription());
        assertTrue(cbOp.getDescription().contains("Callback: myEvent from createPet"));
        assertEquals(List.of("Callbacks"), cbOp.getTags());
    }

    @Test
    void extractCallbackPaths_keepsLiteralExpression() {
        Operation cbOp = new Operation().summary("ExistingSummary").description("Existing").tags(List.of("X"));
        PathItem pi = new PathItem().post(cbOp);
        Callback cb = new Callback();
        cb.addPathItem("https://example.com/hook", pi);
        Map<String, Callback> callbacks = Map.of("evt", cb);

        Map<String, PathItem> result = processor.extractCallbackPaths(callbacks, "opName", new OpenAPI());

        assertTrue(result.containsKey("https://example.com/hook"));
        assertEquals("ExistingSummary", cbOp.getSummary());
        assertTrue(cbOp.getDescription().contains("[Callback: evt from opName]"));
        assertTrue(cbOp.getDescription().contains("Existing"));
        assertEquals(List.of("X"), cbOp.getTags());
    }

    @Test
    void extractCallbackPaths_skipsNullCallback() {
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        callbacks.put("x", null);
        Map<String, PathItem> result = processor.extractCallbackPaths(callbacks, "op", new OpenAPI());
        assertTrue(result.isEmpty());
    }
}
