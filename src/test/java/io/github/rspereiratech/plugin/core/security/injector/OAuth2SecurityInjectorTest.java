package io.github.rspereiratech.plugin.core.security.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.security.model.EnvironmentVariable;
import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;

import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;

class OAuth2SecurityInjectorTest {

    private final OAuth2SecurityInjector injector = new OAuth2SecurityInjector();

    @Test
    void supports_onlyOAuth2() {
        assertTrue(injector.supports(new SecurityScheme().type(SecurityScheme.Type.OAUTH2)));
        assertFalse(injector.supports(new SecurityScheme().type(SecurityScheme.Type.HTTP)));
    }

    @Test
    void inject_withoutFlows_producesOnlyAccessTokenVar() {
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.OAUTH2);
        SecurityInjection r = injector.inject(s, "oauth");

        assertEquals(1, r.headers().size());
        assertEquals("Bearer {{oauthAccessToken}}", r.headers().get(0).value());
        assertEquals(1, r.variables().size());
        assertEquals("oauthAccessToken", r.variables().get(0).name());
    }

    @Test
    void inject_withClientCredentialsFlowAddsTokenUrlAndScopes() {
        Scopes scopes = new Scopes();
        scopes.addString("read", "Read");
        scopes.addString("write", "Write");
        OAuthFlow ccFlow = new OAuthFlow().tokenUrl("https://x.example/token").scopes(scopes);
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().clientCredentials(ccFlow));

        SecurityInjection r = injector.inject(s, "oauth");
        List<EnvironmentVariable> vars = r.variables();
        assertTrue(vars.stream().anyMatch(v -> v.name().equals("oauthClientCredentialsTokenUrl")
                && v.placeholder().equals("https://x.example/token")));
        assertTrue(vars.stream().anyMatch(v -> v.name().equals("oauthScopes")
                && v.placeholder().contains("read")));
    }

    @Test
    void inject_emptyScopes_doesNotAddScopesVar() {
        OAuthFlow flow = new OAuthFlow().tokenUrl("u");
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().password(flow));
        SecurityInjection r = injector.inject(s, "o");
        assertFalse(r.variables().stream().anyMatch(v -> v.name().equals("oScopes")));
    }

    @Test
    void inject_missingTokenUrl_usesPlaceholder() {
        OAuthFlow flow = new OAuthFlow();
        SecurityScheme s = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(flow));
        SecurityInjection r = injector.inject(s, "o");
        assertTrue(r.variables().stream().anyMatch(v -> v.name().equals("oAuthCodeTokenUrl")
                && v.placeholder().equals("<token-url>")));
    }
}
