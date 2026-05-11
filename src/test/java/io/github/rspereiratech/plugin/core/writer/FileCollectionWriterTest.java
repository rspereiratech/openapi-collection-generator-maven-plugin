package io.github.rspereiratech.plugin.core.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.rspereiratech.plugin.core.config.GenerationConfig;
import io.github.rspereiratech.plugin.core.model.CollectionFormat;

class FileCollectionWriterTest {

    private final FileCollectionWriter writer = new FileCollectionWriter();

    @Test
    void write_writesJsonToBuildOutputDir(@TempDir Path dir) throws Exception {
        File outDir = dir.resolve("out").toFile();
        GenerationConfig cfg = new GenerationConfig(outDir, CollectionFormat.POSTMAN, "My API");
        File out = writer.write("{\"hello\":1}", cfg);

        assertTrue(out.exists());
        assertEquals("{\"hello\":1}", Files.readString(out.toPath()));
        assertTrue(out.getName().startsWith("postman_"));
        // sanitization replaces space with underscore
        assertTrue(out.getName().contains("My_API"));
    }

    @Test
    void write_defaultsToCollectionName_whenNull(@TempDir Path dir) throws Exception {
        GenerationConfig cfg = new GenerationConfig(dir.toFile(), CollectionFormat.INSOMNIA, null);
        File out = writer.write("{}", cfg);
        assertTrue(out.getName().startsWith("insomnia_collection"));
    }

    @Test
    void write_createsDirectoryIfMissing(@TempDir Path dir) throws Exception {
        File deep = dir.resolve("a/b/c").toFile();
        GenerationConfig cfg = new GenerationConfig(deep, CollectionFormat.POSTMAN, "Api");
        File out = writer.write("{}", cfg);
        assertTrue(out.exists());
        assertTrue(deep.exists());
    }

    @Test
    void write_throwsWriterException_whenDirectoryCannotBeCreated(@TempDir Path dir) throws Exception {
        // Create a regular file at the target path so mkdirs() fails
        File blocking = dir.resolve("blocker").toFile();
        Files.writeString(blocking.toPath(), "x");
        File problematic = new File(blocking, "sub");
        GenerationConfig cfg = new GenerationConfig(problematic, CollectionFormat.POSTMAN, "n");
        assertThrows(WriterException.class, () -> writer.write("{}", cfg));
    }
}
