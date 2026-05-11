package io.github.rspereiratech.plugin.postman.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import io.github.rspereiratech.plugin.core.security.model.HttpQueryParam;
import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;
import io.github.rspereiratech.plugin.postman.model.PostmanUrl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

class PostmanUrlBuilderTest {

    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final PostmanUrlBuilder builder = new PostmanUrlBuilder(sec);

    @Test
    void build_pathSegmentsConvertedFromBracesToColon() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        PostmanUrl u = builder.build("/pets/{id}/items", new Operation(), "b", new OpenAPI());
        assertEquals(List.of("pets", ":id", "items"), u.path());
        assertTrue(u.raw().startsWith("{{baseUrl}}"));
        assertTrue(u.raw().contains(":id"));
        assertEquals(List.of("{{baseUrl}}"), u.host());
    }

    @Test
    void build_appendsQueryParams() {
        Operation op = new Operation();
        op.addParametersItem(new QueryParameter().name("limit").description("max"));
        op.addParametersItem(new HeaderParameter().name("X-skip"));
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());

        PostmanUrl u = builder.build("/p", op, "b", new OpenAPI());
        assertEquals(1, u.query().size());
        assertEquals("limit", u.query().get(0).key());
        assertTrue(u.raw().contains("?limit="));
    }

    @Test
    void build_addsSecurityQueryParams() {
        Operation op = new Operation();
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection(
                List.of(), List.of(new HttpQueryParam("apikey", "{{v}}")), List.of()));

        PostmanUrl u = builder.build("/p", op, "b", new OpenAPI());
        assertEquals(1, u.query().size());
        assertEquals("apikey", u.query().get(0).key());
        assertEquals("security", u.query().get(0).description());
    }

    @Test
    void build_emptyPath_producesNoSegments() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        PostmanUrl u = builder.build("/", new Operation(), "b", new OpenAPI());
        // host is always {{baseUrl}}
        assertEquals(List.of("{{baseUrl}}"), u.host());
    }
}
