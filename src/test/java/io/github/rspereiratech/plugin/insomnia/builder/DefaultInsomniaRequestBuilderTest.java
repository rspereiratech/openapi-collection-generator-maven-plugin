package io.github.rspereiratech.plugin.insomnia.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.deprecated.DeprecationMarker;
import io.github.rspereiratech.plugin.core.extension.ExtensionProcessorChain;
import io.github.rspereiratech.plugin.core.extension.ExtensionResult;
import io.github.rspereiratech.plugin.core.id.IdGenerator;
import io.github.rspereiratech.plugin.core.link.LinkDescriptionEnricher;
import io.github.rspereiratech.plugin.insomnia.body.InsomniaBodyBuilder;
import io.github.rspereiratech.plugin.insomnia.header.InsomniaHeaderBuilder;
import io.github.rspereiratech.plugin.insomnia.model.InsomniaRequest;
import io.github.rspereiratech.plugin.insomnia.parameter.InsomniaParameterBuilder;
import io.github.rspereiratech.plugin.insomnia.url.InsomniaUrlResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class DefaultInsomniaRequestBuilderTest {

    private final IdGenerator id = mock(IdGenerator.class);
    private final InsomniaUrlResolver url = mock(InsomniaUrlResolver.class);
    private final InsomniaHeaderBuilder header = mock(InsomniaHeaderBuilder.class);
    private final InsomniaBodyBuilder body = mock(InsomniaBodyBuilder.class);
    private final InsomniaParameterBuilder params = mock(InsomniaParameterBuilder.class);
    private final DeprecationMarker depr = mock(DeprecationMarker.class);
    private final ExtensionProcessorChain extChain = mock(ExtensionProcessorChain.class);
    private final LinkDescriptionEnricher linkEnricher = mock(LinkDescriptionEnricher.class);

    private final DefaultInsomniaRequestBuilder builder = new DefaultInsomniaRequestBuilder(
            id, url, header, body, params, depr, extChain, linkEnricher);

    @Test
    void build_usesSummaryAsName_whenPresent() {
        Operation op = new Operation().summary("Get pet").description("desc");
        when(extChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(linkEnricher.enrich(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        when(depr.markName(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(depr.markDescription(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(id.generate(eq("req"), anyString())).thenReturn("req_1");
        when(url.resolve(anyString(), any())).thenReturn("{{ base_url }}/pet");

        InsomniaRequest r = builder.build("/pet", "GET", op, "parent", new OpenAPI());

        assertEquals("Get pet", r.name());
        assertEquals("desc", r.description());
        assertEquals("parent", r.parentId());
        assertEquals("GET", r.method());
        assertEquals("req_1", r.id());
    }

    @Test
    void build_fallsBackToMethodAndPath_whenSummaryMissing() {
        Operation op = new Operation();
        when(extChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(linkEnricher.enrich(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        when(depr.markName(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(depr.markDescription(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(id.generate(any(), any())).thenReturn("req_x");
        when(url.resolve(any(), any())).thenReturn("u");

        InsomniaRequest r = builder.build("/x", "POST", op, "p", new OpenAPI());
        assertEquals("POST /x", r.name());
    }

    @Test
    void build_appliesExtensionOverridesAndDeprecation() {
        Operation op = new Operation().deprecated(true).summary("Orig");
        when(extChain.process(any())).thenReturn(new ExtensionResult("Renamed", "appended"));
        when(linkEnricher.enrich(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        when(depr.markName(anyString(), eq(true))).thenAnswer(i -> "DEP_" + i.getArgument(0));
        when(depr.markDescription(anyString(), eq(true))).thenAnswer(i -> "WARN: " + i.getArgument(0));
        when(id.generate(any(), any())).thenReturn("req_z");
        when(url.resolve(any(), any())).thenReturn("u");

        InsomniaRequest r = builder.build("/x", "GET", op, "parent", new OpenAPI());

        assertEquals("DEP_Renamed", r.name());
        assertTrue(r.description().startsWith("WARN:"));
        assertTrue(r.description().contains("appended"));
    }

    @Test
    void build_collectsLinksFromResponses() {
        Operation op = new Operation().summary("S");
        ApiResponse resp = new ApiResponse();
        resp.setLinks(Map.of("L", new Link().operationId("op1")));
        op.responses(new ApiResponses().addApiResponse("200", resp));
        when(extChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(linkEnricher.enrich(anyString(), any())).thenAnswer(i -> "enriched-" + i.getArgument(0));
        when(depr.markName(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(depr.markDescription(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(id.generate(any(), any())).thenReturn("req_e");
        when(url.resolve(any(), any())).thenReturn("u");
        when(header.build(any(), any())).thenReturn(List.of());
        when(params.build(any(), any())).thenReturn(List.of());

        InsomniaRequest r = builder.build("/x", "GET", op, "p", new OpenAPI());
        assertTrue(r.description().startsWith("enriched-"));
    }
}
