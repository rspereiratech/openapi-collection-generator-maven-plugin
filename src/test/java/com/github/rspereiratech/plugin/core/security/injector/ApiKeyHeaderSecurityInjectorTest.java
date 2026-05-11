package com.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.SecurityScheme;

class ApiKeyHeaderSecurityInjectorTest {

    private final ApiKeyHeaderSecurityInjector injector = new ApiKeyHeaderSecurityInjector();

    @Test
    void supports_onlyApiKeyHeader() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER);
        assertTrue(injector.supports(s));
        SecurityScheme other = new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.QUERY);
        assertFalse(injector.supports(other));
    }

    @Test
    void inject_returnsHeaderWithVariable() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER).name("X-API-KEY");
        SecurityInjection r = injector.inject(s, "auth");

        assertEquals(1, r.headers().size());
        assertEquals("X-API-KEY", r.headers().get(0).name());
        assertEquals("{{authValue}}", r.headers().get(0).value());
        assertEquals("authValue", r.variables().get(0).name());
    }
}
