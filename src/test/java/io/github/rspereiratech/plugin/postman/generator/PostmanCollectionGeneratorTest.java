package io.github.rspereiratech.plugin.postman.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.plugin.core.config.GenerationConfig;
import io.github.rspereiratech.plugin.core.generator.AdditionalFile;
import io.github.rspereiratech.plugin.core.generator.CollectionGenerationException;
import io.github.rspereiratech.plugin.core.model.CollectionFormat;
import io.github.rspereiratech.plugin.core.security.applier.SecurityApplier;
import io.github.rspereiratech.plugin.core.security.model.EnvironmentVariable;
import io.github.rspereiratech.plugin.core.security.model.SecurityInjection;
import io.github.rspereiratech.plugin.core.serializer.CollectionSerializer;
import io.github.rspereiratech.plugin.core.server.ServerEnvironment;
import io.github.rspereiratech.plugin.core.server.ServerEnvironmentGenerator;
import io.github.rspereiratech.plugin.postman.grouper.OperationGrouper;
import io.github.rspereiratech.plugin.postman.model.PostmanItem;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

class PostmanCollectionGeneratorTest {

    private final OperationGrouper grouper = mock(OperationGrouper.class);
    private final CollectionSerializer serializer = mock(CollectionSerializer.class);
    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final ServerEnvironmentGenerator serverGen = mock(ServerEnvironmentGenerator.class);

    private final PostmanCollectionGenerator gen =
            new PostmanCollectionGenerator(grouper, serializer, sec, serverGen);

    @Test
    void generate_returnsSerializedString() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("T").description("D"));
        when(serverGen.generate(any(), anyString()))
                .thenReturn(List.of(new ServerEnvironment("Prod", "https://x", "f.json")));
        Map<String, List<PostmanItem>> groups = new LinkedHashMap<>();
        groups.put("Pets", List.of(PostmanItem.request("getPet", null)));
        when(grouper.group(any(), anyString())).thenReturn(groups);
        when(sec.applyGlobal(any())).thenReturn(new SecurityInjection(List.of(), List.of(),
                List.of(new EnvironmentVariable("token", "<v>"))));
        when(serializer.serialize(any())).thenReturn("SERIALIZED");

        String r = gen.generate(api, new GenerationConfig(null, CollectionFormat.POSTMAN, "MyApi"));
        assertEquals("SERIALIZED", r);
    }

    @Test
    void generate_usesConfigName_overTitle() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("T"));
        when(serverGen.generate(any(), anyString()))
                .thenReturn(List.of(new ServerEnvironment("Prod", "u", "f")));
        when(grouper.group(any(), anyString())).thenReturn(Map.of());
        when(sec.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(serializer.serialize(any())).thenReturn("X");

        gen.generate(api, new GenerationConfig(null, CollectionFormat.POSTMAN, "Custom"));
        org.mockito.Mockito.verify(serverGen).generate(api, "Custom");
    }

    @Test
    void generate_usesBaseUrlFallback_whenNoServers() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("T"));
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());
        when(grouper.group(any(), anyString())).thenReturn(Map.of());
        when(sec.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(serializer.serialize(any())).thenReturn("X");

        gen.generate(api, new GenerationConfig(null, CollectionFormat.POSTMAN, "n"));
        org.mockito.Mockito.verify(grouper).group(api, "{{baseUrl}}");
    }

    @Test
    void generate_wrapsErrorsInCollectionGenerationException() {
        OpenAPI api = new OpenAPI().info(new Info().title("T"));
        when(serverGen.generate(any(), anyString())).thenThrow(new RuntimeException("x"));
        assertThrows(CollectionGenerationException.class,
                () -> gen.generate(api, new GenerationConfig(null, CollectionFormat.POSTMAN, "n")));
    }

    @Test
    void generateAdditionalFiles_returnsOneFilePerServer() throws Exception {
        OpenAPI api = new OpenAPI().info(new Info().title("T"));
        when(serverGen.generate(any(), anyString())).thenReturn(List.of(
                new ServerEnvironment("Prod", "https://p", "Prod.environment.json"),
                new ServerEnvironment("Stg", "https://s", "Stg.environment.json")));
        when(sec.applyGlobal(any())).thenReturn(new SecurityInjection(List.of(), List.of(),
                List.of(new EnvironmentVariable("tok", "<v>"))));

        List<AdditionalFile> files = gen.generateAdditionalFiles(api,
                new GenerationConfig(null, CollectionFormat.POSTMAN, "n"));
        assertEquals(2, files.size());
        assertNotNull(files.get(0).content());
        assertTrue(files.get(0).content().contains("baseUrl"));
        assertTrue(files.get(0).content().contains("tok"));
    }

    @Test
    void generateAdditionalFiles_wrapsErrors() {
        OpenAPI api = new OpenAPI().info(new Info().title("T"));
        when(serverGen.generate(any(), anyString())).thenThrow(new RuntimeException("boom"));
        assertThrows(CollectionGenerationException.class,
                () -> gen.generateAdditionalFiles(api,
                        new GenerationConfig(null, CollectionFormat.POSTMAN, "n")));
    }
}
