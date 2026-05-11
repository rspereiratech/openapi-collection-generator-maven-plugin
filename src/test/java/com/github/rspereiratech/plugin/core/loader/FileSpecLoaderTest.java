package com.github.rspereiratech.plugin.core.loader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSpecLoaderTest {

    private final FileSpecLoader loader = new FileSpecLoader();

    @Test
    void validate_throws_whenNull() {
        MojoExecutionException ex = assertThrows(MojoExecutionException.class,
                () -> loader.validate(null));
        assertTrue(ex.getMessage().contains("Spec not found"));
    }

    @Test
    void validate_throws_whenFileMissing(@TempDir Path dir) {
        File missing = new File(dir.toFile(), "missing.yaml");
        MojoExecutionException ex = assertThrows(MojoExecutionException.class,
                () -> loader.validate(missing));
        assertTrue(ex.getMessage().contains("Spec not found"));
    }

    @Test
    void validate_throws_whenNotARegularFile(@TempDir Path dir) {
        MojoExecutionException ex = assertThrows(MojoExecutionException.class,
                () -> loader.validate(dir.toFile()));
        assertTrue(ex.getMessage().contains("Spec is not a file"));
    }

    @Test
    void validate_succeeds_forRegularFile(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("spec.yaml");
        Files.writeString(f, "openapi: 3.0.0");
        assertDoesNotThrow(() -> loader.validate(f.toFile()));
    }
}
