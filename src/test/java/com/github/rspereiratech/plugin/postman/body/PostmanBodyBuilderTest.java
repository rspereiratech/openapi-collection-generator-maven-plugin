package com.github.rspereiratech.plugin.postman.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.example.SchemaExampleGenerator;
import com.github.rspereiratech.plugin.postman.model.PostmanBody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

class PostmanBodyBuilderTest {

    private final SchemaExampleGenerator gen = mock(SchemaExampleGenerator.class);
    private final PostmanBodyBuilder builder = new PostmanBodyBuilder(gen);

    @Test
    void build_returnsNull_whenNoRequestBody() {
        assertNull(builder.build(new Operation(), new OpenAPI()));
    }

    @Test
    void build_returnsNull_whenContentEmpty() {
        assertNull(builder.build(new Operation().requestBody(new RequestBody().content(new Content())), new OpenAPI()));
    }

    @Test
    void build_prefersNamedExamples() {
        Map<String, Example> exs = new LinkedHashMap<>();
        exs.put("default", new Example().value(Map.of("id", 1)));
        MediaType mt = new MediaType().examples(exs);
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));

        PostmanBody body = builder.build(op, new OpenAPI());
        assertNotNull(body);
        assertEquals("raw", body.mode());
        assertTrue(body.raw().contains("\"id\" : 1"));
        assertEquals("json", body.options().raw().language());
    }

    @Test
    void build_usesSingleExample_whenNoNamedExamples() {
        MediaType mt = new MediaType().example(Map.of("k", "v"));
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));

        PostmanBody body = builder.build(op, new OpenAPI());
        assertTrue(body.raw().contains("\"k\" : \"v\""));
    }

    @Test
    void build_fallsBackToSchemaGenerator() {
        MediaType mt = new MediaType().schema(new ObjectSchema());
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));
        when(gen.generate(any(), any())).thenReturn(Map.of("a", "b"));

        PostmanBody body = builder.build(op, new OpenAPI());
        assertTrue(body.raw().contains("\"a\" : \"b\""));
    }
}
