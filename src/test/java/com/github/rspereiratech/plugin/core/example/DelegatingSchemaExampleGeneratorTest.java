package com.github.rspereiratech.plugin.core.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;

class DelegatingSchemaExampleGeneratorTest {

    private final SchemaExampleGenerator delegate = mock(SchemaExampleGenerator.class);

    @Test
    void generate_throws_whenDelegateNotConfigured() {
        DelegatingSchemaExampleGenerator g = new DelegatingSchemaExampleGenerator();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> g.generate(new StringSchema(), new OpenAPI()));
        assertEquals("Delegate not configured", ex.getMessage());
    }

    @Test
    void generate_forwardsToDelegate_onceSet() {
        DelegatingSchemaExampleGenerator g = new DelegatingSchemaExampleGenerator();
        g.setDelegate(delegate);
        when(delegate.generate(any(), any())).thenReturn("v");
        assertEquals("v", g.generate(new StringSchema(), new OpenAPI()));
    }
}
