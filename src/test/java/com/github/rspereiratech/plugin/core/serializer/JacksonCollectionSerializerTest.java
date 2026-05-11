package com.github.rspereiratech.plugin.core.serializer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class JacksonCollectionSerializerTest {

    private final JacksonCollectionSerializer serializer = new JacksonCollectionSerializer();

    @Test
    void serialize_producesPrettyJson() throws Exception {
        String json = serializer.serialize(Map.of("k", "v"));
        assertTrue(json.contains("\"k\""));
        assertTrue(json.contains("\"v\""));
        // pretty-printed output contains newlines
        assertTrue(json.contains("\n"));
    }

    @Test
    void serialize_throwsSerializationException_forUnserializable() {
        Object loop = new Object() {
            @SuppressWarnings("unused")
            public Object getSelf() { return this; }
        };
        assertThrows(SerializationException.class, () -> serializer.serialize(loop));
    }
}
