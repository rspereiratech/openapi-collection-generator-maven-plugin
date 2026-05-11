package io.github.rspereiratech.plugin.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerator;
import io.github.rspereiratech.openapi.collection.generator.core.model.CollectionFormat;
import io.github.rspereiratech.openapi.collection.generator.insomnia.generator.InsomniaCollectionGenerator;
import io.github.rspereiratech.openapi.collection.generator.postman.generator.PostmanCollectionGenerator;

class DefaultCollectionGeneratorFactoryTest {

    private final DefaultCollectionGeneratorFactory factory = new DefaultCollectionGeneratorFactory();

    @Test
    void create_returnsPostmanGenerator() {
        CollectionGenerator g = factory.create(CollectionFormat.POSTMAN);
        assertNotNull(g);
        assertTrue(g instanceof PostmanCollectionGenerator);
    }

    @Test
    void create_returnsInsomniaGenerator() {
        CollectionGenerator g = factory.create(CollectionFormat.INSOMNIA);
        assertNotNull(g);
        assertTrue(g instanceof InsomniaCollectionGenerator);
    }
}
