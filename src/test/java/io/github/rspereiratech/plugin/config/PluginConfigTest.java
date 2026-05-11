package io.github.rspereiratech.plugin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.config.GenerationConfig;
import io.github.rspereiratech.openapi.collection.generator.core.model.CollectionFormat;

class PluginConfigTest {

    @Test
    void toGenerationConfig_copiesOutputDirFormatAndName() {
        File spec = new File("spec.yaml");
        File out = new File("out");
        PluginConfig cfg = new PluginConfig(spec, out, CollectionFormat.POSTMAN, "MyApi", "https://x");

        GenerationConfig gc = cfg.toGenerationConfig();

        assertSame(out, gc.outputDirectory());
        assertEquals(CollectionFormat.POSTMAN, gc.format());
        assertEquals("MyApi", gc.collectionName());
    }

    @Test
    void toGenerationConfig_allowsNullCollectionName() {
        PluginConfig cfg = new PluginConfig(new File("s"), new File("o"),
                CollectionFormat.INSOMNIA, null, null);
        GenerationConfig gc = cfg.toGenerationConfig();
        assertEquals(CollectionFormat.INSOMNIA, gc.format());
        assertEquals(null, gc.collectionName());
    }
}
