package io.github.rspereiratech.plugin.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.swagger.v3.oas.models.OpenAPI;

class SwaggerOpenApiParserTest {

    private final SwaggerOpenApiParser parser = new SwaggerOpenApiParser();

    @Test
    void parse_succeeds_forValidSpec(@TempDir Path dir) throws Exception {
        String yaml = "openapi: 3.0.0\n"
                + "info:\n  title: Test API\n  version: '1.0'\n"
                + "paths:\n  /ping:\n    get:\n      summary: Ping\n      responses:\n        '200':\n          description: ok\n";
        File spec = dir.resolve("spec.yaml").toFile();
        Files.writeString(spec.toPath(), yaml);

        OpenAPI api = parser.parse(spec);
        assertNotNull(api);
        assertEquals("Test API", api.getInfo().getTitle());
    }

    @Test
    void parse_throws_forNonexistentFile(@TempDir Path dir) {
        File missing = new File(dir.toFile(), "nope.yaml");
        assertThrows(OpenApiParseException.class, () -> parser.parse(missing));
    }

    @Test
    void parse_throws_forInvalidContent(@TempDir Path dir) throws Exception {
        File spec = dir.resolve("invalid.yaml").toFile();
        Files.writeString(spec.toPath(), "not openapi content :::");
        assertThrows(OpenApiParseException.class, () -> parser.parse(spec));
    }
}
