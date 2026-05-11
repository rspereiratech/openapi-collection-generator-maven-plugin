package com.github.rspereiratech.plugin.core.security.applier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.security.model.EnvironmentVariable;
import com.github.rspereiratech.plugin.core.security.model.SecurityInjection;
import com.github.rspereiratech.plugin.core.security.resolver.SecuritySchemeResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

class DefaultSecurityApplierTest {

    private final SecuritySchemeResolver resolver = mock(SecuritySchemeResolver.class);
    private final DefaultSecurityApplier applier = new DefaultSecurityApplier(resolver);

    @Test
    void apply_delegatesToResolver() {
        Operation op = new Operation();
        OpenAPI api = new OpenAPI();
        SecurityInjection inj = new SecurityInjection();
        when(resolver.resolve(op, api)).thenReturn(inj);
        assertSame(inj, applier.apply(op, api));
    }

    @Test
    void applyGlobal_returnsEmpty_whenPathsNull() {
        SecurityInjection r = applier.applyGlobal(new OpenAPI());
        assertTrue(r.headers().isEmpty());
        assertTrue(r.queryParams().isEmpty());
        assertTrue(r.variables().isEmpty());
    }

    @Test
    void applyGlobal_aggregatesUniqueVariablesAcrossOperations() {
        Operation op1 = new Operation();
        Operation op2 = new Operation();
        PathItem p1 = new PathItem().get(op1);
        PathItem p2 = new PathItem().post(op2);
        OpenAPI api = new OpenAPI().paths(new Paths());
        api.getPaths().addPathItem("/a", p1);
        api.getPaths().addPathItem("/b", p2);

        when(resolver.resolve(any(), any())).thenReturn(
                new SecurityInjection(List.of(), List.of(),
                        List.of(new EnvironmentVariable("token", "<v1>"))),
                new SecurityInjection(List.of(), List.of(),
                        List.of(new EnvironmentVariable("token", "<dup>"),
                                new EnvironmentVariable("apikey", "<v2>"))));

        SecurityInjection global = applier.applyGlobal(api);
        assertTrue(global.headers().isEmpty());
        assertTrue(global.queryParams().isEmpty());
        assertEquals(2, global.variables().size());
        // First occurrence wins
        assertEquals("<v1>", global.variables().get(0).placeholder());
    }
}
