package com.github.rspereiratech.plugin.insomnia.builder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.rspereiratech.plugin.core.deprecated.DeprecationMarker;
import com.github.rspereiratech.plugin.core.extension.ExtensionContext;
import com.github.rspereiratech.plugin.core.extension.ExtensionProcessorChain;
import com.github.rspereiratech.plugin.core.id.IdGenerator;
import com.github.rspereiratech.plugin.insomnia.body.InsomniaBodyBuilder;
import com.github.rspereiratech.plugin.insomnia.header.InsomniaHeaderBuilder;
import com.github.rspereiratech.plugin.insomnia.parameter.InsomniaParameterBuilder;
import com.github.rspereiratech.plugin.insomnia.url.InsomniaUrlResolver;
import com.github.rspereiratech.plugin.core.link.LinkDescriptionEnricher;
import com.github.rspereiratech.plugin.insomnia.model.InsomniaRequest;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.links.Link;

/**
 * Default implementation of {@link InsomniaRequestBuilder} that assembles an Insomnia request
 * by delegating URL resolution, header/body/parameter building, deprecation marking,
 * extension processing, and link enrichment to dedicated collaborators.
 */
public class DefaultInsomniaRequestBuilder implements InsomniaRequestBuilder {

    private final IdGenerator id;
    private final InsomniaUrlResolver url;
    private final InsomniaHeaderBuilder header;
    private final InsomniaBodyBuilder body;
    private final InsomniaParameterBuilder params;
    private final DeprecationMarker depr;
    private final ExtensionProcessorChain extChain;
    private final LinkDescriptionEnricher linkEnricher;

    /**
     * Constructs a new request builder with all required collaborators.
     *
     * @param id           generator for unique resource identifiers
     * @param url          resolver for building Insomnia-compatible URLs
     * @param header       builder for request headers
     * @param body         builder for request bodies
     * @param params       builder for query parameters
     * @param depr         marker for deprecated operations
     * @param extChain     chain for processing vendor extensions
     * @param linkEnricher enricher for appending link descriptions
     */
    public DefaultInsomniaRequestBuilder(IdGenerator id, InsomniaUrlResolver url, InsomniaHeaderBuilder header,
            InsomniaBodyBuilder body, InsomniaParameterBuilder params, DeprecationMarker depr,
            ExtensionProcessorChain extChain, LinkDescriptionEnricher linkEnricher) {
        this.id = id;
        this.url = url;
        this.header = header;
        this.body = body;
        this.params = params;
        this.depr = depr;
        this.extChain = extChain;
        this.linkEnricher = linkEnricher;
    }

    @Override
    public InsomniaRequest build(String path, String method, Operation op, String parentId, OpenAPI openApi) {
        boolean deprecated = Boolean.TRUE.equals(op.getDeprecated());
        String rawName = Optional.ofNullable(op.getSummary())
            .orElse(method + " " + path);
        String rawDesc = Optional.ofNullable(op.getDescription()).orElse("");
        var ext = extChain.process(
            new ExtensionContext(path, method, rawName, rawDesc, op));
        String name = ext.nameOverride() != null ? ext.nameOverride() : rawName;
        String desc = ext.descriptionAppend() != null
            ? rawDesc + "\n\n" + ext.descriptionAppend() : rawDesc;
        desc = linkEnricher.enrich(desc, collectLinks(op));
        name = depr.markName(name, deprecated);
        desc = depr.markDescription(desc, deprecated);
        return new InsomniaRequest(
            id.generate("req", method + path), InsomniaRequest.TYPE,
            parentId, name, method, url.resolve(path, op),
            body.build(op, openApi), header.build(op, openApi),
            params.build(op, openApi), desc);
    }

    /**
     * Collects all links defined across the operation's responses.
     *
     * @param op the OpenAPI operation
     * @return a map of link names to link definitions, preserving insertion order
     */
    private Map<String, Link> collectLinks(Operation op) {
        if (op.getResponses() == null) return Map.of();
        return op.getResponses().values().stream()
            .filter(r -> r.getLinks() != null)
            .flatMap(r -> r.getLinks().entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue,
                (a, b) -> a, LinkedHashMap::new));
    }
}
