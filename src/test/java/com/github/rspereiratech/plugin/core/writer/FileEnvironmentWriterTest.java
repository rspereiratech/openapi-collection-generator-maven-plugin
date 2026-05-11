package com.github.rspereiratech.plugin.core.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.rspereiratech.plugin.core.config.GenerationConfig;
import com.github.rspereiratech.plugin.core.generator.AdditionalFile;
import com.github.rspereiratech.plugin.core.model.CollectionFormat;

class FileEnvironmentWriterTest {

    private final FileEnvironmentWriter writer = new FileEnvironmentWriter();

    @Test
    void writeAll_writesEachFile(@TempDir Path dir) throws Exception {
        GenerationConfig cfg = new GenerationConfig(dir.toFile(), CollectionFormat.POSTMAN, "api");
        List<File> result = writer.writeAll(List.of(
                new AdditionalFile("a.json", "{\"x\":1}"),
                new AdditionalFile("b.json", "{}")), cfg);

        assertEquals(2, result.size());
        assertEquals("{\"x\":1}", Files.readString(result.get(0).toPath()));
        assertEquals("{}", Files.readString(result.get(1).toPath()));
    }

    @Test
    void writeAll_createsDirectoryIfMissing(@TempDir Path dir) throws Exception {
        File outDir = dir.resolve("nested/out").toFile();
        GenerationConfig cfg = new GenerationConfig(outDir, CollectionFormat.POSTMAN, "api");
        List<File> result = writer.writeAll(List.of(new AdditionalFile("x.json", "v")), cfg);
        assertTrue(result.get(0).exists());
        assertTrue(outDir.exists());
    }

    @Test
    void writeAll_returnsEmpty_whenInputEmpty(@TempDir Path dir) throws Exception {
        GenerationConfig cfg = new GenerationConfig(dir.toFile(), CollectionFormat.POSTMAN, "api");
        assertTrue(writer.writeAll(List.of(), cfg).isEmpty());
    }

    @Test
    void writeAll_throwsWriterException_whenDirectoryCreationFails(@TempDir Path dir) throws Exception {
        File blocking = dir.resolve("file").toFile();
        Files.writeString(blocking.toPath(), "x");
        File problematic = new File(blocking, "sub");
        GenerationConfig cfg = new GenerationConfig(problematic, CollectionFormat.POSTMAN, "api");
        assertThrows(WriterException.class, () -> writer.writeAll(
                List.of(new AdditionalFile("x.json", "v")), cfg));
    }
}
