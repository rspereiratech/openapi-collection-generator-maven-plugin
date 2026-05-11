package com.github.rspereiratech.plugin.core.schema.discriminator;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.schema.ref.SchemaRefResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

class DefaultDiscriminatorResolverTest {

    private final SchemaRefResolver ref = mock(SchemaRefResolver.class);
    private final DefaultDiscriminatorResolver resolver = new DefaultDiscriminatorResolver(ref);

    @Test
    void resolve_byMappingFirstValue() {
        Schema<?> resolved = new ObjectSchema();
        Discriminator disc = new Discriminator();
        disc.setMapping(Map.of("Cat", "#/components/schemas/Cat"));
        doReturn(resolved).when(ref).resolveByName(eq("Cat"), any());

        Schema<?> result = resolver.resolve(List.of(new ObjectSchema()), disc, new OpenAPI());
        assertSame(resolved, result);
    }

    @Test
    void resolve_fallsBackToFirstSchema_whenMappingResolutionFails() {
        Schema<?> first = new ObjectSchema();
        Discriminator disc = new Discriminator();
        disc.setMapping(Map.of("X", "#/components/schemas/X"));
        doReturn(null).when(ref).resolveByName(eq("X"), any());
        doReturn(first).when(ref).resolve(first, null);

        Schema<?> result = resolver.resolve(List.of(first), disc, null);
        assertSame(first, result);
    }

    @Test
    void resolve_usesFirstCandidate_whenDiscriminatorNull() {
        Schema<?> first = new ObjectSchema();
        doReturn(first).when(ref).resolve(first, null);

        Schema<?> result = resolver.resolve(List.of(first), null, null);
        assertSame(first, result);
    }

    @Test
    void resolve_returnsNull_whenSchemasEmpty() {
        assertNull(resolver.resolve(List.of(), null, null));
    }

    @Test
    void resolve_returnsNull_whenSchemasNull() {
        assertNull(resolver.resolve(null, null, null));
    }

    @Test
    void resolve_emptyMapping_usesFirstCandidate() {
        Schema<?> first = new ObjectSchema();
        Discriminator disc = new Discriminator();
        doReturn(first).when(ref).resolve(first, null);
        Schema<?> result = resolver.resolve(List.of(first), disc, null);
        assertSame(first, result);
    }
}
