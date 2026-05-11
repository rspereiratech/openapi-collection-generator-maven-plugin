package io.github.rspereiratech.plugin.insomnia.parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import io.github.rspereiratech.plugin.core.security.model.HttpQueryParam;
import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;
import io.github.rspereiratech.plugin.insomnia.model.InsomniaParameter;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

class DefaultInsomniaParameterBuilderTest {

    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final DefaultInsomniaParameterBuilder builder = new DefaultInsomniaParameterBuilder(sec);

    @Test
    void build_extractsQueryParamsAndIgnoresOthers() {
        Operation op = new Operation();
        op.addParametersItem(new QueryParameter().name("q").description("d"));
        op.addParametersItem(new HeaderParameter().name("h"));
        when(sec.apply(op, null)).thenReturn(new SecurityInjection());

        List<InsomniaParameter> r = builder.build(op, null);
        assertEquals(1, r.size());
        assertEquals("q", r.get(0).name());
        assertEquals("d", r.get(0).description());
    }

    @Test
    void build_appendsSecurityQueryParams() {
        Operation op = new Operation();
        when(sec.apply(op, null)).thenReturn(new SecurityInjection(
                List.of(), List.of(new HttpQueryParam("apikey", "{{x}}")), List.of()));

        List<InsomniaParameter> r = builder.build(op, null);
        assertEquals(1, r.size());
        assertEquals("apikey", r.get(0).name());
        assertEquals("{{x}}", r.get(0).value());
        assertEquals("security", r.get(0).description());
    }

    @Test
    void build_handlesQueryParamWithoutDescription() {
        Operation op = new Operation();
        op.addParametersItem(new QueryParameter().name("q"));
        when(sec.apply(op, null)).thenReturn(new SecurityInjection());

        List<InsomniaParameter> r = builder.build(op, null);
        assertEquals("", r.get(0).description());
    }

    @Test
    void build_returnsUnmodifiableList() {
        Operation op = new Operation();
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        List<InsomniaParameter> r = builder.build(op, null);
        assertTrue(r.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> r.add(new InsomniaParameter("x", "", "")));
    }
}
