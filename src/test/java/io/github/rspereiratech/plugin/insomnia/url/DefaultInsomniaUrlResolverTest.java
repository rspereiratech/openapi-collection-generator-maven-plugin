package io.github.rspereiratech.plugin.insomnia.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.applier.SecurityApplier;

import io.swagger.v3.oas.models.Operation;

class DefaultInsomniaUrlResolverTest {

    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final DefaultInsomniaUrlResolver resolver = new DefaultInsomniaUrlResolver(sec);

    @Test
    void resolve_prefixesBaseUrlTemplate() {
        assertEquals("{{ base_url }}/pets", resolver.resolve("/pets", new Operation()));
    }

    @Test
    void resolve_convertsPathParamsToColonSyntax() {
        assertEquals("{{ base_url }}/pets/:id/items/:slug",
                resolver.resolve("/pets/{id}/items/{slug}", new Operation()));
    }

    @Test
    void resolve_emptyPath() {
        assertEquals("{{ base_url }}", resolver.resolve("", new Operation()));
    }
}
