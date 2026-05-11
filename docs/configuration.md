# Configuration

The plugin exposes a single Mojo, `generate`, declared in `GenerateCollectionMojo`. It is bound by default to the Maven `generate-resources` phase.

## Parameters

All parameters can be set either inside `<configuration>` in `pom.xml` or as system properties on the command line.

| Parameter         | Property                  | Default                                                | Required | Description                                                                |
|-------------------|---------------------------|--------------------------------------------------------|----------|----------------------------------------------------------------------------|
| `specFile`        | `openapi.spec`            | `${project.basedir}/src/main/resources/openapi.yaml`   | Yes      | Path to the OpenAPI specification file (YAML or JSON).                     |
| `outputDirectory` | `openapi.outputDir`       | `${project.build.directory}/generated-collections`     | No       | Output directory for generated collection and environment files.           |
| `format`          | `openapi.format`          | `POSTMAN`                                              | No       | Target collection format. One of `POSTMAN` or `INSOMNIA` (case-insensitive). |
| `collectionName`  | `openapi.collectionName`  | API title from the spec                                | No       | Optional collection name override.                                         |
| `baseUrl`         | `openapi.baseUrl`         | First server URL from the spec                         | No       | Optional base URL override applied to the generated collection.            |

## Plugin declaration

```xml
<plugin>
    <groupId>io.github.rspereiratech</groupId>
    <artifactId>openapi-collection-generator-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <specFile>${project.basedir}/src/main/resources/openapi.yaml</specFile>
        <outputDirectory>${project.build.directory}/collections</outputDirectory>
        <format>POSTMAN</format>
        <collectionName>My API</collectionName>
        <baseUrl>https://api.example.com</baseUrl>
    </configuration>
</plugin>
```

## Command-line invocation

```bash
mvn io.github.rspereiratech:openapi-collection-generator-maven-plugin:generate \
    -Dopenapi.spec=src/main/resources/petstore.yaml \
    -Dopenapi.format=INSOMNIA \
    -Dopenapi.outputDir=target/collections \
    -Dopenapi.collectionName=Petstore \
    -Dopenapi.baseUrl=https://petstore.example.com
```

## Validation

Before parsing, the plugin validates that `specFile`:

1. Exists on disk.
2. Is a regular file (not a directory).

If either check fails, the build fails with a `MojoExecutionException`. See `FileSpecLoader`.

## Format selection

`format` is parsed by `CollectionFormat.fromString(String)`. Both upper- and lower-case values are accepted. An unknown value triggers a build failure.

| Value      | Generator                       |
|------------|---------------------------------|
| `POSTMAN`  | `PostmanCollectionGenerator`    |
| `INSOMNIA` | `InsomniaCollectionGenerator`   |
