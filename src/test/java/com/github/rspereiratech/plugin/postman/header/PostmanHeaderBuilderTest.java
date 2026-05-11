package com.github.rspereiratech.plugin.postman.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import com.github.rspereiratech.plugin.core.security.model.HttpHeader;
import com.github.rspereiratech.plugin.core.security.model.SecurityInjection;
import com.github.rspereiratech.plugin.postman.model.PostmanHeader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

class PostmanHeaderBuilderTest {

    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final PostmanHeaderBuilder builder = new PostmanHeaderBuilder(sec);

    @Test
    void build_includesHeaderParams() {
        Operation op = new Operation();
        op.addParametersItem(new HeaderParameter().name("X-A"));
        op.addParametersItem(new QueryParameter().name("q"));
        when(sec.apply(op, null)).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(op, null);
        assertEquals(1, headers.size());
        assertEquals("X-A", headers.get(0).key());
    }

    @Test
    void build_addsContentType() {
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", new MediaType())));
        when(sec.apply(op, null)).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(op, null);
        assertEquals("Content-Type", headers.get(0).key());
        assertEquals("application/json", headers.get(0).value());
    }

    @Test
    void build_addsSecurityHeaders() {
        Operation op = new Operation();
        when(sec.apply(op, null)).thenReturn(new SecurityInjection(
                List.of(new HttpHeader("Authorization", "Bearer x")), List.of(), List.of()));

        List<PostmanHeader> headers = builder.build(op, null);
        assertEquals("Authorization", headers.get(0).key());
    }

    @Test
    void build_unmodifiable() {
        Operation op = new Operation();
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        List<PostmanHeader> headers = builder.build(op, null);
        assertTrue(headers.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> headers.add(new PostmanHeader("a", "b")));
    }
}
