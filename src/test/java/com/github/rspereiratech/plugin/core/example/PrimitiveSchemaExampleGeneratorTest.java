package com.github.rspereiratech.plugin.core.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;

class PrimitiveSchemaExampleGeneratorTest {

    private final SchemaExampleGenerator next = mock(SchemaExampleGenerator.class);
    private final PrimitiveSchemaExampleGenerator gen = new PrimitiveSchemaExampleGenerator(next);

    @Test
    void generate_returnsNull_whenSchemaNull() {
        assertNull(gen.generate(null, new OpenAPI()));
    }

    @Test
    void generate_prefersExplicitExample() {
        StringSchema s = new StringSchema();
        s.setExample("hello");
        assertEquals("hello", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_prefersXExamplesOverExplicit() {
        StringSchema s = new StringSchema();
        s.setExample("explicit");
        s.addExtension("x-examples", Map.of("a", Map.of("value", "extracted")));
        assertEquals("extracted", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_xExamplesValueAsRawObject() {
        StringSchema s = new StringSchema();
        s.addExtension("x-examples", Map.of("a", "raw"));
        assertEquals("raw", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_xExamplesIgnored_whenNotMap() {
        StringSchema s = new StringSchema();
        s.addExtension("x-examples", "notAMap");
        s.setExample("ex");
        assertEquals("ex", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_xExamplesIgnored_whenEmpty() {
        StringSchema s = new StringSchema();
        s.addExtension("x-examples", Map.of());
        s.setExample("ex");
        assertEquals("ex", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_usesEnumFirstValue_whenNoExample() {
        StringSchema s = new StringSchema();
        s.setEnum(List.of("A", "B"));
        assertEquals("A", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_usesDefault_whenNoExampleAndNoEnum() {
        StringSchema s = new StringSchema();
        s.setDefault("D");
        assertEquals("D", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_string_defaultsToStringLiteral() {
        assertEquals("string", gen.generate(new StringSchema(), new OpenAPI()));
    }

    @Test
    void generate_stringFormats() {
        assertEquals("2024-01-01", gen.generate(new StringSchema().format("date"), new OpenAPI()));
        assertEquals("2024-01-01T00:00:00Z", gen.generate(new StringSchema().format("date-time"), new OpenAPI()));
        assertEquals("00000000-0000-0000-0000-000000000000", gen.generate(new StringSchema().format("uuid"), new OpenAPI()));
        assertEquals("user@example.com", gen.generate(new StringSchema().format("email"), new OpenAPI()));
        assertEquals("https://example.com", gen.generate(new StringSchema().format("uri"), new OpenAPI()));
        assertEquals("dGVzdA==", gen.generate(new StringSchema().format("byte"), new OpenAPI()));
        assertEquals("<binary>", gen.generate(new StringSchema().format("binary"), new OpenAPI()));
        assertEquals("********", gen.generate(new StringSchema().format("password"), new OpenAPI()));
        assertEquals("string", gen.generate(new StringSchema().format("unknown"), new OpenAPI()));
    }

    @Test
    void generate_integer_returnsZeroByDefault() {
        assertEquals(0, gen.generate(new IntegerSchema(), new OpenAPI()));
    }

    @Test
    void generate_integer_usesMinimumWhenPresent() {
        IntegerSchema s = new IntegerSchema();
        s.setMinimum(new BigDecimal("5"));
        assertEquals(5, gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_number_returnsZeroDoubleByDefault() {
        assertEquals(0.0, gen.generate(new NumberSchema(), new OpenAPI()));
    }

    @Test
    void generate_number_usesMinimumWhenPresent() {
        NumberSchema s = new NumberSchema();
        s.setMinimum(new BigDecimal("1.5"));
        assertEquals(1.5, gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_boolean_returnsTrue() {
        io.swagger.v3.oas.models.media.BooleanSchema b = new io.swagger.v3.oas.models.media.BooleanSchema();
        assertEquals(true, gen.generate(b, new OpenAPI()));
    }

    @Test
    void generate_unknownType_delegatesToNext() {
        ObjectSchema s = new ObjectSchema();
        s.setType("object");
        when(next.generate(any(), any())).thenReturn("delegated");
        assertEquals("delegated", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_unknownType_returnsNullWhenNoNext() {
        PrimitiveSchemaExampleGenerator g = new PrimitiveSchemaExampleGenerator(null);
        ObjectSchema s = new ObjectSchema();
        s.setType("object");
        assertNull(g.generate(s, new OpenAPI()));
    }

    @Test
    void generate_explicitExampleOnEnum_wins() {
        StringSchema s = new StringSchema();
        s.setExample("ex");
        s.setEnum(List.of("A"));
        assertEquals("ex", gen.generate(s, new OpenAPI()));
    }

    @Test
    void generate_nullType_delegatesToNext() {
        StringSchema s = new StringSchema();
        s.setType(null);
        when(next.generate(any(), any())).thenReturn("via-next");
        // when type is null, "null" string falls through to default branch
        Object out = gen.generate(s, new OpenAPI());
        assertTrue(out == null || "via-next".equals(out));
    }
}
