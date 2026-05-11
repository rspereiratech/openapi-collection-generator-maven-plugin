package io.github.rspereiratech.plugin.core.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;

class ArraySchemaExampleGeneratorTest {

    private final SchemaExampleGenerator next = mock(SchemaExampleGenerator.class);
    private final SchemaExampleGenerator recursive = mock(SchemaExampleGenerator.class);
    private final ArraySchemaExampleGenerator gen = new ArraySchemaExampleGenerator(next, recursive);

    @Test
    void generate_delegatesToNext_whenNotArraySchema() {
        ObjectSchema schema = new ObjectSchema();
        when(next.generate(any(), any())).thenReturn("delegated");

        Object result = gen.generate(schema, new OpenAPI());

        assertEquals("delegated", result);
    }

    @Test
    void generate_returnsNull_whenNotArrayAndNoNext() {
        ArraySchemaExampleGenerator g = new ArraySchemaExampleGenerator(null, recursive);
        assertNull(g.generate(new ObjectSchema(), new OpenAPI()));
    }

    @Test
    void generate_returnsSingleElementList_fromRecursiveItem() {
        ArraySchema arr = new ArraySchema();
        StringSchema item = new StringSchema();
        arr.setItems(item);
        when(recursive.generate(any(), any())).thenReturn("hello");

        Object result = gen.generate(arr, new OpenAPI());

        assertTrue(result instanceof List<?>);
        assertEquals(List.of("hello"), result);
    }

    @Test
    void generate_returnsSingletonGenericObject_whenArrayHasNoItems() {
        ArraySchema arr = new ArraySchema();
        arr.setItems(null);

        Object result = gen.generate(arr, new OpenAPI());

        assertTrue(result instanceof List<?>);
        assertEquals(1, ((List<?>) result).size());
        assertNotNull(((List<?>) result).get(0));
    }

    @Test
    void generate_returnsSingletonGenericObject_whenRecursiveReturnsNull() {
        ArraySchema arr = new ArraySchema();
        StringSchema item = new StringSchema();
        arr.setItems(item);
        when(recursive.generate(any(), any())).thenReturn(null);

        Object result = gen.generate(arr, new OpenAPI());

        assertTrue(result instanceof List<?>);
        assertEquals(1, ((List<?>) result).size());
        assertNotNull(((List<?>) result).get(0));
    }
}
