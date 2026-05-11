package io.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.SecurityScheme;

class BearerSecurityInjectorTest {

    private final BearerSecurityInjector injector = new BearerSecurityInjector();

    @Test
    void supports_httpBearerCaseInsensitive() {
        assertTrue(injector.supports(new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")));
        assertTrue(injector.supports(new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("Bearer")));
        assertFalse(injector.supports(new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }

    @Test
    void inject_returnsBearerHeaderWithTokenVar() {
        SecurityInjection r = injector.inject(
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer"), "auth");

        assertEquals("Authorization", r.headers().get(0).name());
        assertEquals("Bearer {{authToken}}", r.headers().get(0).value());
        assertEquals("authToken", r.variables().get(0).name());
    }
}
