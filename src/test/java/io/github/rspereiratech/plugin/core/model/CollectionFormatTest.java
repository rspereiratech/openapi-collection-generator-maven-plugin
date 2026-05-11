package io.github.rspereiratech.plugin.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CollectionFormatTest {

    @Test
    void fromString_acceptsUpperCase() {
        assertEquals(CollectionFormat.POSTMAN, CollectionFormat.fromString("POSTMAN"));
        assertEquals(CollectionFormat.INSOMNIA, CollectionFormat.fromString("INSOMNIA"));
    }

    @Test
    void fromString_acceptsLowerAndMixedCase() {
        assertEquals(CollectionFormat.POSTMAN, CollectionFormat.fromString("postman"));
        assertEquals(CollectionFormat.INSOMNIA, CollectionFormat.fromString("Insomnia"));
    }

    @Test
    void fromString_throws_onUnknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> CollectionFormat.fromString("bogus"));
        assertTrue(ex.getMessage().contains("bogus"));
        assertTrue(ex.getMessage().contains("POSTMAN"));
    }

    @Test
    void fromString_throws_onNull() {
        assertThrows(NullPointerException.class, () -> CollectionFormat.fromString(null));
    }
}
