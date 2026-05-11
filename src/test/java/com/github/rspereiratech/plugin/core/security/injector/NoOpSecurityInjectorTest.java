package com.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.SecurityScheme;

class NoOpSecurityInjectorTest {

    private final NoOpSecurityInjector injector = new NoOpSecurityInjector();

    @Test
    void supports_alwaysTrue() {
        assertTrue(injector.supports(new SecurityScheme()));
        assertTrue(injector.supports(null));
    }

    @Test
    void inject_returnsEmpty() {
        SecurityInjection r = injector.inject(new SecurityScheme(), "n");
        assertTrue(r.headers().isEmpty());
        assertTrue(r.queryParams().isEmpty());
        assertTrue(r.variables().isEmpty());
    }
}
