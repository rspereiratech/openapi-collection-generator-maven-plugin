package com.github.rspereiratech.plugin.factory;

import java.util.List;

import com.github.rspereiratech.plugin.core.factory.CollectionGeneratorFactory;
import com.github.rspereiratech.plugin.core.callback.DefaultCallbackProcessor;
import com.github.rspereiratech.plugin.insomnia.deprecated.InsomniaDeprecationMarker;
import com.github.rspereiratech.plugin.core.example.ArraySchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.example.ComposedSchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.example.DelegatingSchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.example.NullableSchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.example.ObjectSchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.example.PrimitiveSchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.example.SchemaExampleGenerator;
import com.github.rspereiratech.plugin.core.extension.ExtensionProcessorChain;
import com.github.rspereiratech.plugin.core.extension.impl.XBetaExtensionProcessor;
import com.github.rspereiratech.plugin.core.extension.impl.XDeprecatedSinceExtensionProcessor;
import com.github.rspereiratech.plugin.core.extension.impl.XInternalExtensionProcessor;
import com.github.rspereiratech.plugin.core.extension.impl.XSummaryExtensionProcessor;
import com.github.rspereiratech.plugin.core.generator.CollectionGenerator;
import com.github.rspereiratech.plugin.insomnia.generator.InsomniaCollectionGenerator;
import com.github.rspereiratech.plugin.core.id.UUIDGenerator;
import com.github.rspereiratech.plugin.insomnia.body.DefaultInsomniaBodyBuilder;
import com.github.rspereiratech.plugin.insomnia.builder.DefaultInsomniaRequestBuilder;
import com.github.rspereiratech.plugin.insomnia.header.DefaultInsomniaHeaderBuilder;
import com.github.rspereiratech.plugin.insomnia.parameter.DefaultInsomniaParameterBuilder;
import com.github.rspereiratech.plugin.insomnia.url.DefaultInsomniaUrlResolver;
import com.github.rspereiratech.plugin.core.link.DefaultLinkDescriptionEnricher;
import com.github.rspereiratech.plugin.core.model.CollectionFormat;
import com.github.rspereiratech.plugin.postman.body.PostmanBodyBuilder;
import com.github.rspereiratech.plugin.postman.builder.PostmanItemBuilder;
import com.github.rspereiratech.plugin.postman.deprecated.PostmanDeprecationMarker;
import com.github.rspereiratech.plugin.postman.generator.PostmanCollectionGenerator;
import com.github.rspereiratech.plugin.postman.grouper.TagOperationGrouper;
import com.github.rspereiratech.plugin.postman.header.PostmanHeaderBuilder;
import com.github.rspereiratech.plugin.postman.url.PostmanUrlBuilder;
import com.github.rspereiratech.plugin.core.schema.discriminator.DefaultDiscriminatorResolver;
import com.github.rspereiratech.plugin.core.schema.ref.DefaultSchemaRefResolver;
import com.github.rspereiratech.plugin.core.security.applier.DefaultSecurityApplier;
import com.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import com.github.rspereiratech.plugin.core.security.factory.DefaultSecurityInjectorFactory;
import com.github.rspereiratech.plugin.core.security.resolver.DefaultSecuritySchemeResolver;
import com.github.rspereiratech.plugin.core.serializer.JacksonCollectionSerializer;
import com.github.rspereiratech.plugin.core.server.DefaultServerEnvironmentGenerator;

/**
 * Default {@link CollectionGeneratorFactory} implementation that assembles the full
 * dependency graph for Postman and Insomnia collection generators.
 */
public class DefaultCollectionGeneratorFactory implements CollectionGeneratorFactory {

    @Override
    public CollectionGenerator create(CollectionFormat format) {
        return switch (format) {
            case POSTMAN  -> buildPostmanGenerator();
            case INSOMNIA -> buildInsomniaGenerator();
        };
    }

    /**
     * Builds a fully wired Postman collection generator with all required collaborators.
     *
     * @return a configured Postman {@link CollectionGenerator}
     */
    private CollectionGenerator buildPostmanGenerator() {
        var security = buildSecurityApplier();
        var exampleGen = buildExampleChain();
        var serializer = new JacksonCollectionSerializer();
        var serverGen = new DefaultServerEnvironmentGenerator();
        var extChain = buildExtensionChain();
        var linkEnricher = new DefaultLinkDescriptionEnricher();
        var deprMarker = new PostmanDeprecationMarker();
        var cbProcessor = new DefaultCallbackProcessor();
        var itemBuilder = new PostmanItemBuilder(
                new PostmanUrlBuilder(security), new PostmanHeaderBuilder(security),
                new PostmanBodyBuilder(exampleGen), deprMarker, extChain, linkEnricher);
        return new PostmanCollectionGenerator(
                new TagOperationGrouper(itemBuilder, cbProcessor),
                serializer, security, serverGen);
    }

    /**
     * Builds a fully wired Insomnia collection generator with all required collaborators.
     *
     * @return a configured Insomnia {@link CollectionGenerator}
     */
    private CollectionGenerator buildInsomniaGenerator() {
        var security = buildSecurityApplier();
        var exampleGen = buildExampleChain();
        var idGenerator = new UUIDGenerator();
        var serializer = new JacksonCollectionSerializer();
        var serverGen = new DefaultServerEnvironmentGenerator();
        var extChain = buildExtensionChain();
        var linkEnricher = new DefaultLinkDescriptionEnricher();
        var deprMarker = new InsomniaDeprecationMarker();
        var requestBuilder = new DefaultInsomniaRequestBuilder(
                idGenerator, new DefaultInsomniaUrlResolver(security),
                new DefaultInsomniaHeaderBuilder(security),
                new DefaultInsomniaBodyBuilder(exampleGen),
                new DefaultInsomniaParameterBuilder(security),
                deprMarker, extChain, linkEnricher);
        return new InsomniaCollectionGenerator(
                idGenerator, requestBuilder, serializer, security, serverGen);
    }

    /**
     * Creates the security applier pipeline used by both Postman and Insomnia generators.
     *
     * @return a configured {@link SecurityApplier}
     */
    private SecurityApplier buildSecurityApplier() {
        return new DefaultSecurityApplier(
                new DefaultSecuritySchemeResolver(new DefaultSecurityInjectorFactory()));
    }

    /**
     * Builds the chain of schema example generators that handle primitives, arrays, objects,
     * composed schemas, and nullable wrappers.
     *
     * @return the root {@link SchemaExampleGenerator} of the chain
     */
    private SchemaExampleGenerator buildExampleChain() {
        var refResolver = new DefaultSchemaRefResolver();
        var discResolver = new DefaultDiscriminatorResolver(refResolver);
        var recursive = new DelegatingSchemaExampleGenerator();
        var primitive = new PrimitiveSchemaExampleGenerator(null);
        var array = new ArraySchemaExampleGenerator(primitive, recursive);
        var object = new ObjectSchemaExampleGenerator(array, refResolver, recursive);
        var composed = new ComposedSchemaExampleGenerator(
                object, refResolver, discResolver, recursive);
        var nullable = new NullableSchemaExampleGenerator(composed);
        recursive.setDelegate(nullable);
        return nullable;
    }

    /**
     * Builds the chain of OpenAPI extension processors (x-summary, x-internal, x-beta, etc.).
     *
     * @return a configured {@link ExtensionProcessorChain}
     */
    private ExtensionProcessorChain buildExtensionChain() {
        return new ExtensionProcessorChain(List.of(
                new XSummaryExtensionProcessor(),
                new XInternalExtensionProcessor(),
                new XBetaExtensionProcessor(),
                new XDeprecatedSinceExtensionProcessor()));
    }
}
