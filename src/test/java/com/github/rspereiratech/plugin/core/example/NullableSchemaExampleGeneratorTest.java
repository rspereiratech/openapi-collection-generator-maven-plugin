package com.github.rspereiratech.plugin.core.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;

class NullableSchemaExampleGeneratorTest {

    private final SchemaExampleGenerator next = mock(SchemaExampleGenerator.class);

    @Test
    void generate_delegatesToNext_whenAvailable() {
        when(next.generate(any(), any())).thenReturn("ok");
        assertEquals("ok", new NullableSchemaExampleGenerator(next).generate(new StringSchema(), new OpenAPI()));
    }

    @Test
    void generate_returnsNull_whenNoNext() {
        assertNull(new NullableSchemaExampleGenerator(null).generate(new StringSchema(), new OpenAPI()));
    }
}
