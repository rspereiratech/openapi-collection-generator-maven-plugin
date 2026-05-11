package com.github.rspereiratech.plugin.core.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.links.Link;

class DefaultLinkDescriptionEnricherTest {

    private final DefaultLinkDescriptionEnricher enricher = new DefaultLinkDescriptionEnricher();

    @Test
    void enrich_returnsOriginalDesc_whenLinksNull() {
        assertEquals("orig", enricher.enrich("orig", null));
    }

    @Test
    void enrich_returnsOriginalDesc_whenLinksEmpty() {
        assertEquals("orig", enricher.enrich("orig", Map.of()));
    }

    @Test
    void enrich_appendsLinkSection_toExistingDesc() {
        Map<String, Link> links = new LinkedHashMap<>();
        links.put("getPet", new Link().operationId("getPet").description("Lookup pet"));
        String result = enricher.enrich("Existing", links);
        assertTrue(result.startsWith("Existing\n\n"));
        assertTrue(result.contains("Related Operations"));
        assertTrue(result.contains("getPet"));
        assertTrue(result.contains("Lookup pet"));
    }

    @Test
    void enrich_returnsJustSection_whenDescBlank() {
        Map<String, Link> links = Map.of("L", new Link());
        String result = enricher.enrich("", links);
        assertTrue(result.startsWith("**Related Operations:**"));
    }

    @Test
    void enrich_handlesLinkWithoutOperationIdOrDescription() {
        Map<String, Link> links = Map.of("L", new Link());
        String result = enricher.enrich("", links);
        assertTrue(result.contains("- **L**"));
    }
}
