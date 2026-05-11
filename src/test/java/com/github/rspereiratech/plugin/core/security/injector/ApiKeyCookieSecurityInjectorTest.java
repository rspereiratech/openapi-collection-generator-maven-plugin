package com.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.SecurityScheme;

class ApiKeyCookieSecurityInjectorTest {

    private final ApiKeyCookieSecurityInjector injector = new ApiKeyCookieSecurityInjector();

    @Test
    void supports_onlyApiKeyCookie() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE);
        assertTrue(injector.supports(s));

        SecurityScheme other = new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER);
        assertFalse(injector.supports(other));
    }

    @Test
    void inject_returnsCookieHeaderAndVariable() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE).name("session");
        SecurityInjection r = injector.inject(s, "auth");

        assertEquals(1, r.headers().size());
        assertEquals("Cookie", r.headers().get(0).name());
        assertTrue(r.headers().get(0).value().contains("session={{authCookie}}"));
        assertEquals(1, r.variables().size());
        assertEquals("authCookie", r.variables().get(0).name());
    }
}
