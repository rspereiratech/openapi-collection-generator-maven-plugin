package io.github.rspereiratech.plugin.core.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

class DefaultServerEnvironmentGeneratorTest {

    private final DefaultServerEnvironmentGenerator gen = new DefaultServerEnvironmentGenerator();

    @Test
    void generate_returnsLocalDefault_whenNoServersDefined() {
        List<ServerEnvironment> envs = gen.generate(new OpenAPI(), "MyApi");
        assertEquals(1, envs.size());
        assertEquals("Local", envs.get(0).name());
        assertEquals("http://localhost:8080", envs.get(0).baseUrl());
        assertTrue(envs.get(0).fileName().contains("MyApi"));
        assertTrue(envs.get(0).fileName().endsWith(".environment.json"));
    }

    @Test
    void generate_usesPositionalDefaults_whenDescriptionsAbsent() {
        OpenAPI api = new OpenAPI().servers(List.of(
                new Server().url("https://prod"),
                new Server().url("https://stg"),
                new Server().url("https://dev"),
                new Server().url("https://extra")));

        List<ServerEnvironment> envs = gen.generate(api, "api");
        assertEquals("Production", envs.get(0).name());
        assertEquals("Staging", envs.get(1).name());
        assertEquals("Development", envs.get(2).name());
        assertEquals("Environment 4", envs.get(3).name());
    }

    @Test
    void generate_prefersServerDescription() {
        OpenAPI api = new OpenAPI().servers(List.of(
                new Server().url("https://x").description("Custom Env")));
        ServerEnvironment env = gen.generate(api, "api").get(0);
        assertEquals("Custom Env", env.name());
    }

    @Test
    void generate_sanitizesNamesInFileName() {
        OpenAPI api = new OpenAPI().servers(List.of(new Server().url("u").description("My Env!")));
        ServerEnvironment env = gen.generate(api, "Foo Bar").get(0);
        // unsafe chars become underscores
        assertTrue(env.fileName().contains("Foo_Bar"));
        assertTrue(env.fileName().contains("My_Env_"));
    }
}
