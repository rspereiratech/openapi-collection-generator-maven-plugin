package com.github.rspereiratech.plugin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import com.github.rspereiratech.plugin.core.factory.CollectionGeneratorFactory;
import com.github.rspereiratech.plugin.core.generator.AdditionalFile;
import com.github.rspereiratech.plugin.core.generator.CollectionGenerator;
import com.github.rspereiratech.plugin.core.loader.SpecLoader;
import com.github.rspereiratech.plugin.core.parser.OpenApiParser;
import com.github.rspereiratech.plugin.core.writer.CollectionWriter;
import com.github.rspereiratech.plugin.core.writer.EnvironmentWriter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

class GenerateCollectionMojoTest {

    private final SpecLoader specLoader = mock(SpecLoader.class);
    private final OpenApiParser parser = mock(OpenApiParser.class);
    private final CollectionGeneratorFactory factory = mock(CollectionGeneratorFactory.class);
    private final CollectionWriter writer = mock(CollectionWriter.class);
    private final EnvironmentWriter envWriter = mock(EnvironmentWriter.class);

    private GenerateCollectionMojo newMojo(File spec, File outDir, String format) throws Exception {
        GenerateCollectionMojo m = new GenerateCollectionMojo(specLoader, parser, factory, writer, envWriter);
        setField(m, "specFile", spec);
        setField(m, "outputDirectory", outDir);
        setField(m, "format", format);
        setField(m, "collectionName", "MyApi");
        setField(m, "baseUrl", null);
        return m;
    }

    private static void setField(Object t, String name, Object value) throws Exception {
        Field f = GenerateCollectionMojo.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(t, value);
    }

    @Test
    void execute_runsHappyPath(@TempDir Path dir) throws Exception {
        File spec = dir.resolve("spec.yaml").toFile();
        File outDir = dir.resolve("out").toFile();
        GenerateCollectionMojo m = newMojo(spec, outDir, "POSTMAN");

        OpenAPI api = new OpenAPI().info(new Info().title("T"));
        when(parser.parse(spec)).thenReturn(api);
        CollectionGenerator g = mock(CollectionGenerator.class);
        when(factory.create(any())).thenReturn(g);
        when(g.generate(any(), any())).thenReturn("{}");
        when(g.generateAdditionalFiles(any(), any())).thenReturn(List.of(new AdditionalFile("env.json", "v")));
        File outFile = dir.resolve("col.json").toFile();
        when(writer.write(anyString(), any())).thenReturn(outFile);
        when(envWriter.writeAll(any(), any())).thenReturn(List.of(dir.resolve("env.json").toFile()));

        m.execute();

        verify(specLoader).validate(spec);
        verify(parser).parse(spec);
        verify(envWriter).writeAll(any(), any());
    }

    @Test
    void execute_doesNotWriteEnvironments_whenNoAdditionalFiles(@TempDir Path dir) throws Exception {
        File spec = dir.resolve("s.yaml").toFile();
        GenerateCollectionMojo m = newMojo(spec, dir.toFile(), "INSOMNIA");

        when(parser.parse(any())).thenReturn(new OpenAPI().info(new Info().title("X")));
        CollectionGenerator g = mock(CollectionGenerator.class);
        when(factory.create(any())).thenReturn(g);
        when(g.generate(any(), any())).thenReturn("{}");
        when(g.generateAdditionalFiles(any(), any())).thenReturn(List.of());
        when(writer.write(anyString(), any())).thenReturn(dir.resolve("c.json").toFile());

        m.execute();
        verify(envWriter, never()).writeAll(any(), any());
    }

    @Test
    void execute_wrapsParserError_inMojoExecutionException(@TempDir Path dir) throws Exception {
        File spec = dir.resolve("s.yaml").toFile();
        GenerateCollectionMojo m = newMojo(spec, dir.toFile(), "POSTMAN");

        when(parser.parse(any())).thenThrow(new RuntimeException("bad"));

        MojoExecutionException ex = assertThrows(MojoExecutionException.class, m::execute);
        assertTrue(ex.getMessage().contains("Failed to generate"));
    }

    @Test
    void execute_propagatesValidationFailure(@TempDir Path dir) throws Exception {
        File spec = dir.resolve("s.yaml").toFile();
        GenerateCollectionMojo m = newMojo(spec, dir.toFile(), "POSTMAN");

        doThrow(new MojoExecutionException("missing")).when(specLoader).validate(any());

        assertThrows(MojoExecutionException.class, m::execute);
        verify(parser, never()).parse(any());
    }

    @Test
    void execute_failsForUnknownFormat(@TempDir Path dir) throws Exception {
        File spec = dir.resolve("s.yaml").toFile();
        GenerateCollectionMojo m = newMojo(spec, dir.toFile(), "BOGUS");
        assertThrows(IllegalArgumentException.class, m::execute);
    }

    @Test
    void defaultConstructor_doesNotThrow() {
        new GenerateCollectionMojo();
    }
}
