package io.github.rspereiratech.plugin.core.security.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.injector.ApiKeyHeaderSecurityInjector;
import io.github.rspereiratech.plugin.core.security.injector.BearerSecurityInjector;
import io.github.rspereiratech.plugin.core.security.injector.NoOpSecurityInjector;
import io.github.rspereiratech.plugin.core.security.injector.SecurityInjector;

import io.swagger.v3.oas.models.security.SecurityScheme;

class DefaultSecurityInjectorFactoryTest {

    @Test
    void resolve_returnsFirstSupporting() {
        SecurityInjector a = mock(SecurityInjector.class);
        SecurityInjector b = mock(SecurityInjector.class);
        SecurityScheme s = new SecurityScheme();
        when(a.supports(s)).thenReturn(false);
        when(b.supports(s)).thenReturn(true);

        DefaultSecurityInjectorFactory f = new DefaultSecurityInjectorFactory(List.of(a, b));
        assertSame(b, f.resolve(s));
    }

    @Test
    void resolve_returnsNoOp_whenNoneMatch() {
        SecurityInjector a = mock(SecurityInjector.class);
        when(a.supports(org.mockito.ArgumentMatchers.any(SecurityScheme.class))).thenReturn(false);
        DefaultSecurityInjectorFactory f = new DefaultSecurityInjectorFactory(List.of(a));
        SecurityInjector r = f.resolve(new SecurityScheme());
        assertNotNull(r);
        assertTrue(r instanceof NoOpSecurityInjector);
    }

    @Test
    void defaultConstructor_registersStandardInjectors() {
        DefaultSecurityInjectorFactory f = new DefaultSecurityInjectorFactory();
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer");
        assertTrue(f.resolve(bearer) instanceof BearerSecurityInjector);

        SecurityScheme apikeyHeader = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Api-Key");
        assertTrue(f.resolve(apikeyHeader) instanceof ApiKeyHeaderSecurityInjector);
    }

}
