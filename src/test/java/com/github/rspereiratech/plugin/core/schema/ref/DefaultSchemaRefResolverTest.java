package com.github.rspereiratech.plugin.core.schema.ref;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

class DefaultSchemaRefResolverTest {

    private final DefaultSchemaRefResolver resolver = new DefaultSchemaRefResolver();

    @Test
    void resolve_returnsSameSchema_whenRefNull() {
        Schema<?> s = new ObjectSchema();
        assertSame(s, resolver.resolve(s, new OpenAPI()));
    }

    @Test
    void resolve_returnsNull_whenSchemaNull() {
        assertNull(resolver.resolve(null, new OpenAPI()));
    }

    @Test
    void resolve_followsRefByName() {
        Schema<?> target = new StringSchema();
        OpenAPI api = new OpenAPI().components(new Components().addSchemas("Pet", target));
        Schema<?> withRef = new Schema<>();
        withRef.set$ref("#/components/schemas/Pet");

        assertSame(target, resolver.resolve(withRef, api));
    }

    @Test
    void resolve_returnsOriginal_whenRefMissing() {
        OpenAPI api = new OpenAPI().components(new Components());
        Schema<?> withRef = new Schema<>();
        withRef.set$ref("#/components/schemas/Missing");
        assertSame(withRef, resolver.resolve(withRef, api));
    }

    @Test
    void resolveByName_returnsNull_whenComponentsNull() {
        assertNull(resolver.resolveByName("X", new OpenAPI()));
    }

    @Test
    void resolveByName_returnsNull_whenSchemasNull() {
        OpenAPI api = new OpenAPI().components(new Components());
        assertNull(resolver.resolveByName("X", api));
    }

    @Test
    void resolveByName_findsSchemaByName() {
        Schema<?> target = new ObjectSchema();
        OpenAPI api = new OpenAPI().components(new Components().addSchemas("Foo", target));
        assertSame(target, resolver.resolveByName("Foo", api));
    }
}
