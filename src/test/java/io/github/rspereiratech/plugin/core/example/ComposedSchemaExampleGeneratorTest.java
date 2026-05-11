package io.github.rspereiratech.plugin.core.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.schema.discriminator.DiscriminatorResolver;
import io.github.rspereiratech.plugin.core.schema.ref.SchemaRefResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

class ComposedSchemaExampleGeneratorTest {

    private final SchemaExampleGenerator next = mock(SchemaExampleGenerator.class);
    private final SchemaRefResolver ref = mock(SchemaRefResolver.class);
    private final DiscriminatorResolver disc = mock(DiscriminatorResolver.class);
    private final SchemaExampleGenerator recursive = mock(SchemaExampleGenerator.class);
    private final ComposedSchemaExampleGenerator gen = new ComposedSchemaExampleGenerator(next, ref, disc, recursive);

    @Test
    void generate_returnsNull_whenSchemaNull() {
        assertNull(gen.generate(null, new OpenAPI()));
    }

    @Test
    void generate_delegatesToNext_whenNoComposition() {
        ObjectSchema s = new ObjectSchema();
        doReturn(s).when(ref).resolve(s, null);
        when(next.generate(s, null)).thenReturn("x");

        assertEquals("x", gen.generate(s, null));
    }

    @Test
    void generate_returnsNull_whenNoCompositionAndNoNext() {
        ComposedSchemaExampleGenerator g = new ComposedSchemaExampleGenerator(null, ref, disc, recursive);
        ObjectSchema s = new ObjectSchema();
        doReturn(s).when(ref).resolve(s, null);
        assertNull(g.generate(s, null));
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void generate_mergesAllOfAndProperties() {
        // Distinguish schemas by setting unique titles so equals/hash differ
        Schema a = new ObjectSchema().title("A");
        Schema b = new ObjectSchema().title("B");
        Schema combined = new ObjectSchema().title("combined");
        combined.setAllOf(List.of(a, b));
        combined.addProperty("extra", new StringSchema());

        doReturn(combined).when(ref).resolve(org.mockito.ArgumentMatchers.same(combined), org.mockito.ArgumentMatchers.isNull());
        doReturn(a).when(ref).resolve(org.mockito.ArgumentMatchers.same(a), org.mockito.ArgumentMatchers.isNull());
        doReturn(b).when(ref).resolve(org.mockito.ArgumentMatchers.same(b), org.mockito.ArgumentMatchers.isNull());
        when(recursive.generate(org.mockito.ArgumentMatchers.same(a), org.mockito.ArgumentMatchers.isNull())).thenReturn(Map.of("k1", "v1"));
        when(recursive.generate(org.mockito.ArgumentMatchers.same(b), org.mockito.ArgumentMatchers.isNull())).thenReturn(Map.of("k2", "v2"));
        when(recursive.generate(any(StringSchema.class), any())).thenReturn("propEx");

        Object result = gen.generate(combined, null);

        assertTrue(result instanceof Map<?,?>);
        Map<?,?> m = (Map<?,?>) result;
        assertEquals("v1", m.get("k1"));
        assertEquals("v2", m.get("k2"));
        assertEquals("propEx", m.get("extra"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void generate_returnsNull_whenAllOfMergedEmpty() {
        Schema combined = new ObjectSchema();
        Schema a = new ObjectSchema();
        combined.setAllOf(List.of(a));
        doReturn(combined).when(ref).resolve(combined, null);
        doReturn(a).when(ref).resolve(a, null);
        when(recursive.generate(a, null)).thenReturn("notAMap");

        assertNull(gen.generate(combined, null));
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void generate_oneOf_selectsViaDiscriminator() {
        Schema variant = new ObjectSchema();
        Schema parent = new ObjectSchema();
        parent.setOneOf(List.of(variant));
        doReturn(parent).when(ref).resolve(parent, null);
        doReturn(variant).when(disc).resolve(any(List.class), any(), any());
        when(recursive.generate(variant, null)).thenReturn("picked");

        assertEquals("picked", gen.generate(parent, null));
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void generate_anyOf_returnsNull_whenNothingChosen() {
        Schema parent = new ObjectSchema();
        parent.setAnyOf(List.of(new ObjectSchema()));
        doReturn(parent).when(ref).resolve(parent, null);
        doReturn(null).when(disc).resolve(any(List.class), any(), any());

        assertNull(gen.generate(parent, null));
    }

    @Test
    void generate_handles_refResolverReturningNull() {
        Schema<?> s = new ObjectSchema();
        doReturn(null).when(ref).resolve(s, null);
        when(next.generate(s, null)).thenReturn("nextResult");
        assertEquals("nextResult", gen.generate(s, null));
    }
}
