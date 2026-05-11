package io.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.SecurityScheme;

class ApiKeyQuerySecurityInjectorTest {

    private final ApiKeyQuerySecurityInjector injector = new ApiKeyQuerySecurityInjector();

    @Test
    void supports_onlyApiKeyQuery() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.QUERY);
        assertTrue(injector.supports(s));
        SecurityScheme other = new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER);
        assertFalse(injector.supports(other));
    }

    @Test
    void inject_returnsQueryParam() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.QUERY).name("api_key");
        SecurityInjection r = injector.inject(s, "auth");

        assertTrue(r.headers().isEmpty());
        assertEquals(1, r.queryParams().size());
        assertEquals("api_key", r.queryParams().get(0).name());
        assertEquals("{{authValue}}", r.queryParams().get(0).value());
    }
}
