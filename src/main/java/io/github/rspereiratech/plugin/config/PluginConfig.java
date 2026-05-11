package io.github.rspereiratech.plugin.config;

import java.io.File;

import io.github.rspereiratech.openapi.collection.generator.core.config.GenerationConfig;
import io.github.rspereiratech.openapi.collection.generator.core.model.CollectionFormat;

/**
 * Immutable configuration record carrying all user-provided plugin parameters.
 *
 * @param specFile        path to the OpenAPI specification file
 * @param outputDirectory directory where generated collection files are written
 * @param format          target collection format (e.g. Postman, Insomnia)
 * @param collectionName  optional human-readable name for the generated collection
 * @param baseUrl         optional base URL override for the API
 */
public record PluginConfig(File specFile, File outputDirectory, CollectionFormat format,
                           String collectionName, String baseUrl) {

    /**
     * Converts this plugin config to a core {@link GenerationConfig} for use by generators.
     *
     * @return a new {@link GenerationConfig} with the collection name
     */
    public GenerationConfig toGenerationConfig() {
        return new GenerationConfig(outputDirectory, format, collectionName);
    }
}
