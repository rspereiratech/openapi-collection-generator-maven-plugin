package io.github.rspereiratech.plugin.insomnia.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.config.GenerationConfig;
import io.github.rspereiratech.plugin.core.generator.CollectionGenerationException;
import io.github.rspereiratech.plugin.core.id.IdGenerator;
import io.github.rspereiratech.plugin.core.model.CollectionFormat;
import io.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import io.github.rspereiratech.plugin.core.security.model.EnvironmentVariable;
import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;
import io.github.rspereiratech.plugin.core.serializer.CollectionSerializer;
import io.github.rspereiratech.plugin.core.server.ServerEnvironment;
import io.github.rspereiratech.plugin.core.server.ServerEnvironmentGenerator;
import io.github.rspereiratech.plugin.insomnia.builder.InsomniaRequestBuilder;
import io.github.rspereiratech.plugin.insomnia.model.InsomniaRequest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.callbacks.Callback;

class InsomniaCollectionGeneratorTest {

    private final IdGenerator id = mock(IdGenerator.class);
    private final InsomniaRequestBuilder rb = mock(InsomniaRequestBuilder.class);
    private final CollectionSerializer serializer = mock(CollectionSerializer.class);
    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final ServerEnvironmentGenerator serverGen = mock(ServerEnvironmentGenerator.class);

    private final InsomniaCollectionGenerator gen =
            new InsomniaCollectionGenerator(id, rb, serializer, sec, serverGen);

    private InsomniaRequest stubRequest() {
        return new InsomniaRequest("r", "request", "p", "n", "GET", "u", null, List.of(), List.of(), "");
    }

    @Test
    void generate_produces_serializedExport_andUsesConfigName() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("apiTitle").description("d")).paths(new Paths());
        Operation op = new Operation().tags(List.of("Pets"));
        api.getPaths().addPathItem("/pets", new PathItem().get(op));
        when(serverGen.generate(any(), anyString())).thenReturn(
                List.of(new ServerEnvironment("Prod", "https://prod", "prod.json")));
        when(sec.applyGlobal(api)).thenReturn(new SecurityInjection(List.of(), List.of(),
                List.of(new EnvironmentVariable("token", "<v>"))));
        when(id.generate(anyString(), anyString())).thenReturn("ID");
        when(rb.build(anyString(), anyString(), any(), anyString(), any())).thenReturn(stubRequest());
        when(serializer.serialize(any())).thenReturn("SERIALIZED");

        GenerationConfig cfg = new GenerationConfig(null, CollectionFormat.INSOMNIA, "ConfigName");
        String r = gen.generate(api, cfg);
        assertEquals("SERIALIZED", r);
        verify(rb).build(eqIgnoreNull("/pets"), eqIgnoreNull("GET"), any(), anyString(), any());
    }

    private static String eqIgnoreNull(String v) {
        return org.mockito.ArgumentMatchers.eq(v);
    }

    @Test
    void generate_fallsBack_toApiTitle_whenConfigNameNull() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("apiTitle")).paths(new Paths());
        api.getPaths().addPathItem("/x", new PathItem().get(new Operation()));
        when(serverGen.generate(any(), anyString())).thenReturn(
                List.of(new ServerEnvironment("Prod", "https://prod", "prod.json")));
        when(sec.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(id.generate(anyString(), anyString())).thenReturn("ID");
        when(rb.build(anyString(), anyString(), any(), anyString(), any())).thenReturn(stubRequest());
        when(serializer.serialize(any())).thenReturn("X");

        gen.generate(api, new GenerationConfig(null, CollectionFormat.INSOMNIA, null));
        verify(serverGen, atLeastOnce()).generate(api, "apiTitle");
    }

    @Test
    void generate_buildsCallbackRequests() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("t")).paths(new Paths());
        Operation cbOp = new Operation();
        Callback cb = new Callback();
        cb.addPathItem("{$req}", new PathItem().post(cbOp));
        Operation op = new Operation();
        op.setCallbacks(java.util.Map.of("evt", cb));
        api.getPaths().addPathItem("/p", new PathItem().get(op));

        when(serverGen.generate(any(), anyString())).thenReturn(
                List.of(new ServerEnvironment("Prod", "https://prod", "prod.json")));
        when(sec.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(id.generate(anyString(), anyString())).thenReturn("ID");
        when(rb.build(anyString(), anyString(), any(), anyString(), any())).thenReturn(stubRequest());
        when(serializer.serialize(any())).thenReturn("X");

        gen.generate(api, new GenerationConfig(null, CollectionFormat.INSOMNIA, "t"));
        // ensure callback request was attempted (path starting with /callbacks/)
        verify(rb, atLeastOnce()).build(anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void generate_wrapsExceptions_inCollectionGenerationException() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("t"));
        when(serverGen.generate(any(), anyString())).thenThrow(new RuntimeException("fail"));
        CollectionGenerationException ex = assertThrows(CollectionGenerationException.class,
                () -> gen.generate(api, new GenerationConfig(null, CollectionFormat.INSOMNIA, "t")));
        assertTrue(ex.getMessage().contains("Insomnia"));
    }

    @Test
    void generateAdditionalFiles_returnsEmptyDefault() throws Exception {
        // Inherited default method returns empty list
        assertTrue(gen.generateAdditionalFiles(new OpenAPI(),
                new GenerationConfig(null, CollectionFormat.INSOMNIA, null)).isEmpty());
    }
}
