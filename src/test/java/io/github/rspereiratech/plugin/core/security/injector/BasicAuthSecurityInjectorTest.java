package io.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.model.EnvironmentVariable;
import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.SecurityScheme;

class BasicAuthSecurityInjectorTest {

    private final BasicAuthSecurityInjector injector = new BasicAuthSecurityInjector();

    @Test
    void supports_httpBasic_caseInsensitive() {
        SecurityScheme b1 = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic");
        SecurityScheme b2 = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("BASIC");
        assertTrue(injector.supports(b1));
        assertTrue(injector.supports(b2));

        SecurityScheme bearer = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer");
        assertFalse(injector.supports(bearer));
    }

    @Test
    void inject_returnsBasicAuthHeader_andUserPassVariables() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic");
        SecurityInjection r = injector.inject(s, "auth");

        assertEquals(1, r.headers().size());
        assertEquals("Authorization", r.headers().get(0).name());
        assertTrue(r.headers().get(0).value().contains("{{authUsername}}"));
        assertTrue(r.headers().get(0).value().contains("{{authPassword}}"));

        List<EnvironmentVariable> vars = r.variables();
        assertEquals(2, vars.size());
        assertEquals("authUsername", vars.get(0).name());
        assertEquals("authPassword", vars.get(1).name());
    }
}
