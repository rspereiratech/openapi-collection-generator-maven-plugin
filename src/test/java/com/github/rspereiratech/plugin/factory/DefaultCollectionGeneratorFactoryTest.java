package com.github.rspereiratech.plugin.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.plugin.core.generator.CollectionGenerator;
import com.github.rspereiratech.plugin.core.model.CollectionFormat;
import com.github.rspereiratech.plugin.insomnia.generator.InsomniaCollectionGenerator;
import com.github.rspereiratech.plugin.postman.generator.PostmanCollectionGenerator;

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
