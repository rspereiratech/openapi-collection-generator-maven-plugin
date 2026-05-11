package com.github.rspereiratech.plugin.core.extension;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ExtensionResultTest {

    @Test
    void noChange_returnsAllNullFields() {
        ExtensionResult r = ExtensionResult.noChange();
        assertNull(r.nameOverride());
        assertNull(r.descriptionAppend());
    }
}
