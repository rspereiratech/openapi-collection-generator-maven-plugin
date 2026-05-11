package com.github.rspereiratech.plugin.core.security.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.security.factory.SecurityInjectorFactory;
import com.github.rspereiratech.plugin.core.security.injector.SecurityInjector;
import com.github.rspereiratech.plugin.core.security.model.HttpHeader;
import com.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

class DefaultSecuritySchemeResolverTest {

    private final SecurityInjectorFactory factory = mock(SecurityInjectorFactory.class);
    private final DefaultSecuritySchemeResolver resolver = new DefaultSecuritySchemeResolver(factory);

    @Test
    void resolve_returnsEmpty_whenNoSecurityDefined() {
        SecurityInjection r = resolver.resolve(new Operation(), new OpenAPI());
        assertTrue(r.headers().isEmpty());
    }

    @Test
    void resolve_usesOperationSecurity_overGlobal() {
        SecurityScheme scheme = new SecurityScheme();
        SecurityInjector injector = mock(SecurityInjector.class);
        when(factory.resolve(scheme)).thenReturn(injector);
        when(injector.inject(eq(scheme), eq("op"))).thenReturn(new SecurityInjection(
                List.of(new HttpHeader("Authorization", "v")), List.of(), List.of()));

        OpenAPI api = new OpenAPI().components(new Components().addSecuritySchemes("op", scheme));
        SecurityRequirement req = new SecurityRequirement(); req.addList("op");
        Operation op = new Operation().security(List.of(req));

        SecurityInjection r = resolver.resolve(op, api);
        assertEquals(1, r.headers().size());
        assertEquals("Authorization", r.headers().get(0).name());
    }

    @Test
    void resolve_fallsBackToGlobalSecurity() {
        SecurityScheme scheme = new SecurityScheme();
        SecurityInjector injector = mock(SecurityInjector.class);
        when(factory.resolve(scheme)).thenReturn(injector);
        when(injector.inject(any(), any())).thenReturn(new SecurityInjection(
                List.of(new HttpHeader("X", "1")), List.of(), List.of()));

        SecurityRequirement req = new SecurityRequirement(); req.addList("g");
        OpenAPI api = new OpenAPI()
                .components(new Components().addSecuritySchemes("g", scheme))
                .security(List.of(req));

        SecurityInjection r = resolver.resolve(new Operation(), api);
        assertEquals(1, r.headers().size());
    }

    @Test
    void resolve_skipsMissingSchemes() {
        SecurityRequirement req = new SecurityRequirement(); req.addList("absent");
        OpenAPI api = new OpenAPI().components(new Components()).security(List.of(req));

        SecurityInjection r = resolver.resolve(new Operation(), api);
        assertTrue(r.headers().isEmpty());
    }
}
