package io.github.rspereiratech.plugin;

import java.io.File;
import java.util.List;

import io.github.rspereiratech.plugin.config.PluginConfig;
import io.github.rspereiratech.openapi.collection.generator.core.factory.CollectionGeneratorFactory;
import io.github.rspereiratech.plugin.factory.DefaultCollectionGeneratorFactory;
import io.github.rspereiratech.openapi.collection.generator.core.generator.AdditionalFile;
import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerator;
import io.github.rspereiratech.openapi.collection.generator.core.loader.FileSpecLoader;
import io.github.rspereiratech.openapi.collection.generator.core.loader.SpecLoader;
import io.github.rspereiratech.openapi.collection.generator.core.model.CollectionFormat;
import io.github.rspereiratech.openapi.collection.generator.core.parser.OpenApiParser;
import io.github.rspereiratech.openapi.collection.generator.core.parser.SwaggerOpenApiParser;
import io.github.rspereiratech.openapi.collection.generator.core.writer.CollectionWriter;
import io.github.rspereiratech.openapi.collection.generator.core.writer.EnvironmentWriter;
import io.github.rspereiratech.openapi.collection.generator.core.writer.FileCollectionWriter;
import io.github.rspereiratech.openapi.collection.generator.core.writer.FileEnvironmentWriter;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven Mojo that generates API collection files (Postman, Insomnia) from an OpenAPI specification.
 *
 * <p>Bound by default to the {@code generate-resources} phase. It reads the OpenAPI spec,
 * parses it, delegates to the appropriate {@link CollectionGenerator}, and writes the
 * resulting collection and any additional environment files to the output directory.</p>
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateCollectionMojo extends AbstractMojo {

    /**
     * Path to the OpenAPI specification file.
     */
    @Parameter(property = "openapi.spec",
               defaultValue = "${project.basedir}/src/main/resources/openapi.yaml", required = true)
    private File specFile;

    /**
     * Output directory for the generated collection and environment files.
     */
    @Parameter(property = "openapi.outputDir",
               defaultValue = "${project.build.directory}/generated-collections")
    private File outputDirectory;

    /**
     * Target collection format ({@code POSTMAN} or {@code INSOMNIA}).
     */
    @Parameter(property = "openapi.format", defaultValue = "POSTMAN")
    private String format;

    /**
     * Optional collection name override. Defaults to the API title from the spec.
     */
    @Parameter(property = "openapi.collectionName")
    private String collectionName;

    /**
     * Optional base URL override for the generated collection.
     */
    @Parameter(property = "openapi.baseUrl")
    private String baseUrl;

    /**
     * Loader responsible for validating the spec file on disk.
     */
    private final SpecLoader specLoader;

    /**
     * Parser that converts the spec file into an OpenAPI model.
     */
    private final OpenApiParser parser;

    /**
     * Factory that creates the appropriate collection generator based on the target format.
     */
    private final CollectionGeneratorFactory factory;

    /**
     * Writer that persists the generated collection JSON to the output directory.
     */
    private final CollectionWriter writer;

    /**
     * Writer that persists additional environment files to the output directory.
     */
    private final EnvironmentWriter environmentWriter;

    /**
     * Default constructor that wires all production dependencies.
     */
    public GenerateCollectionMojo() {
        this.specLoader = new FileSpecLoader();
        this.parser = new SwaggerOpenApiParser();
        this.factory = new DefaultCollectionGeneratorFactory();
        this.writer = new FileCollectionWriter();
        this.environmentWriter = new FileEnvironmentWriter();
    }

    /**
     * Constructor for dependency injection, primarily used in tests.
     *
     * @param specLoader        loader responsible for spec file validation
     * @param parser            parser that converts the spec file into an {@link OpenAPI} model
     * @param factory           factory that creates the appropriate {@link CollectionGenerator}
     * @param writer            writer that persists the generated collection JSON
     * @param environmentWriter writer that persists additional environment files
     */
    GenerateCollectionMojo(SpecLoader specLoader, OpenApiParser parser,
                           CollectionGeneratorFactory factory, CollectionWriter writer,
                           EnvironmentWriter environmentWriter) {
        this.specLoader = specLoader;
        this.parser = parser;
        this.factory = factory;
        this.writer = writer;
        this.environmentWriter = environmentWriter;
    }

    /**
     * Executes the collection generation lifecycle: validate spec, parse, generate, and write output.
     *
     * @throws MojoExecutionException if the spec is missing or any step in the generation pipeline fails
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("openapi-collection-plugin: starting generation");
        PluginConfig config = new PluginConfig(specFile, outputDirectory,
                CollectionFormat.fromString(format), collectionName, baseUrl);
        try {
            specLoader.validate(config.specFile());
            OpenAPI openApi = parser.parse(config.specFile());
            getLog().info("Spec loaded: " + openApi.getInfo().getTitle());

            CollectionGenerator generator = factory.create(config.format());

            var generationConfig = config.toGenerationConfig();
            String json = generator.generate(openApi, generationConfig);

            File out = writer.write(json, generationConfig);
            getLog().info("Collection written: " + out.getAbsolutePath());

            List<AdditionalFile> extras = generator.generateAdditionalFiles(openApi, generationConfig);
            if (!extras.isEmpty()) {
                List<File> envFiles = environmentWriter.writeAll(extras, generationConfig);
                envFiles.forEach(f -> getLog().info("Environment written: " + f.getAbsolutePath()));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate collection", e);
        }
    }
}
