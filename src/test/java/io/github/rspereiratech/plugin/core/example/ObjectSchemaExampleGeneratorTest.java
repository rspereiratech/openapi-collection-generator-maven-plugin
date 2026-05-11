package io.github.rspereiratech.plugin.core.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.schema.ref.SchemaRefResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;

class ObjectSchemaExampleGeneratorTest {

    private final SchemaExampleGenerator next = mock(SchemaExampleGenerator.class);
    private final SchemaRefResolver ref = mock(SchemaRefResolver.class);
    private final SchemaExampleGenerator recursive = mock(SchemaExampleGenerator.class);
    private final ObjectSchemaExampleGenerator gen = new ObjectSchemaExampleGenerator(next, ref, recursive);

    @Test
    void generate_returnsNull_whenSchemaNull() {
        assertNull(gen.generate(null, new OpenAPI()));
    }

    @Test
    void generate_delegatesToNext_whenNotObject() {
        StringSchema s = new StringSchema();
        org.mockito.Mockito.doReturn(s).when(ref).resolve(s, null);
        when(next.generate(s, null)).thenReturn("hi");
        assertEquals("hi", gen.generate(s, null));
    }

    @Test
    void generate_delegatesToNext_whenObjectWithoutProperties() {
        ObjectSchema s = new ObjectSchema();
        org.mockito.Mockito.doReturn(s).when(ref).resolve(s, null);
        when(next.generate(s, null)).thenReturn("hi");
        assertEquals("hi", gen.generate(s, null));
    }

    @Test
    void generate_buildsMapOfProperties() {
        ObjectSchema s = new ObjectSchema();
        s.addProperty("name", new StringSchema());
        s.addProperty("age", new StringSchema());
        org.mockito.Mockito.doReturn(s).when(ref).resolve(s, null);
        when(recursive.generate(any(), any())).thenReturn("val");

        Object r = gen.generate(s, null);
        assertTrue(r instanceof Map<?,?>);
        Map<?,?> m = (Map<?,?>) r;
        assertEquals("val", m.get("name"));
        assertEquals("val", m.get("age"));
    }

    @Test
    void generate_handlesNullRefResolution() {
        ObjectSchema s = new ObjectSchema();
        s.addProperty("x", new StringSchema());
        when(ref.resolve(s, null)).thenReturn(null);
        when(recursive.generate(any(), any())).thenReturn("v");

        Object r = gen.generate(s, null);
        assertTrue(r instanceof Map<?,?>);
        assertEquals("v", ((Map<?,?>) r).get("x"));
    }

    @Test
    void generate_returnsNull_whenNotObjectAndNoNext() {
        ObjectSchemaExampleGenerator g = new ObjectSchemaExampleGenerator(null, ref, recursive);
        StringSchema s = new StringSchema();
        org.mockito.Mockito.doReturn(s).when(ref).resolve(s, null);
        assertNull(g.generate(s, null));
    }
}
