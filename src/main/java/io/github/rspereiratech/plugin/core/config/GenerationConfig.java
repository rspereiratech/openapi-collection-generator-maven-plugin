package io.github.rspereiratech.plugin.core.config;

import io.github.rspereiratech.plugin.core.model.CollectionFormat;

import java.io.File;

/**
 * Core configuration parameters used by generators and writers.
 *
 * @param outputDirectory directory where generated files are written
 * @param format          target collection format (e.g. Postman, Insomnia)
 * @param collectionName  optional human-readable name for the generated collection
 */
public record GenerationConfig(File outputDirectory, CollectionFormat format, String collectionName) {}
