package io.github.rspereiratech.plugin.postman.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.deprecated.DeprecationMarker;
import io.github.rspereiratech.plugin.core.extension.ExtensionProcessorChain;
import io.github.rspereiratech.plugin.core.extension.ExtensionResult;
import io.github.rspereiratech.plugin.core.link.LinkDescriptionEnricher;
import io.github.rspereiratech.plugin.postman.body.BodyBuilder;
import io.github.rspereiratech.plugin.postman.header.HeaderBuilder;
import io.github.rspereiratech.plugin.postman.model.PostmanItem;
import io.github.rspereiratech.plugin.postman.model.PostmanUrl;
import io.github.rspereiratech.plugin.postman.url.UrlBuilder;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class PostmanItemBuilderTest {

    private final UrlBuilder url = mock(UrlBuilder.class);
    private final HeaderBuilder header = mock(HeaderBuilder.class);
    private final BodyBuilder body = mock(BodyBuilder.class);
    private final DeprecationMarker depr = mock(DeprecationMarker.class);
    private final ExtensionProcessorChain extChain = mock(ExtensionProcessorChain.class);
    private final LinkDescriptionEnricher linkEnricher = mock(LinkDescriptionEnricher.class);

    private final PostmanItemBuilder builder =
            new PostmanItemBuilder(url, header, body, depr, extChain, linkEnricher);

    private void stubDefaults() {
        when(extChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(linkEnricher.enrich(anyString(), any())).thenAnswer(i -> i.getArgument(0));
        when(depr.markName(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(depr.markDescription(anyString(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(url.build(any(), any(), any(), any())).thenReturn(new PostmanUrl("u", List.of(), List.of(), List.of()));
        when(header.build(any(), any())).thenReturn(List.of());
        when(body.build(any(), any())).thenReturn(null);
    }

    @Test
    void build_usesSummary_orFallsBackToMethodPath() {
        stubDefaults();
        Operation op = new Operation().summary("My Op").description("d");
        PostmanItem item = builder.build("/x", "GET", op, "https://b", new OpenAPI());
        assertEquals("My Op", item.name());
        assertNotNull(item.request());
    }

    @Test
    void build_fallback_methodAndPath_whenNoSummary() {
        stubDefaults();
        Operation op = new Operation();
        PostmanItem item = builder.build("/y", "POST", op, "https://b", new OpenAPI());
        assertEquals("POST /y", item.name());
    }

    @Test
    void build_appliesExtensionOverride() {
        stubDefaults();
        when(extChain.process(any())).thenReturn(new ExtensionResult("New", "extra"));
        Operation op = new Operation().summary("Old");
        PostmanItem item = builder.build("/p", "GET", op, "b", new OpenAPI());
        assertEquals("New", item.name());
        assertTrue(item.request().description().contains("extra"));
    }

    @Test
    void build_marksDeprecated() {
        stubDefaults();
        when(extChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(depr.markName(anyString(), eq(true))).thenReturn("[DEP] N");
        when(depr.markDescription(anyString(), eq(true))).thenReturn("WARN");
        Operation op = new Operation().summary("N").deprecated(true);
        PostmanItem item = builder.build("/p", "GET", op, "b", new OpenAPI());
        assertEquals("[DEP] N", item.name());
        assertEquals("WARN", item.request().description());
    }

    @Test
    void build_enrichesDescriptionWithLinks() {
        stubDefaults();
        when(linkEnricher.enrich(anyString(), any())).thenReturn("ENR");
        Operation op = new Operation().summary("S").description("orig");
        ApiResponse r = new ApiResponse();
        r.setLinks(Map.of("L", new Link()));
        op.responses(new ApiResponses().addApiResponse("200", r));
        PostmanItem item = builder.build("/p", "GET", op, "b", new OpenAPI());
        assertEquals("ENR", item.request().description());
    }
}
