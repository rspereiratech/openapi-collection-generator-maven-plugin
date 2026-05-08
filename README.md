# OpenAPI Collection Generator Maven Plugin

Maven plugin that generates Postman and Insomnia collections from an OpenAPI specification.

## Requirements

- Java 17+
- Maven 3.9+

## Usage

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.rspereiratech</groupId>
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
                <format>POSTMAN</format>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Then run:

```bash
mvn generate-resources
```

Generated files are written to `${project.build.directory}/generated-collections` by default.

## Goal

### `generate`

Reads an OpenAPI specification, parses it, and writes the resulting collection (and any environment files) to the output directory. Bound by default to the `generate-resources` phase.

## Configuration

| Parameter        | Property                  | Default                                                | Description                                                              |
|------------------|---------------------------|--------------------------------------------------------|--------------------------------------------------------------------------|
| `specFile`       | `openapi.spec`            | `${project.basedir}/src/main/resources/openapi.yaml`   | Path to the OpenAPI specification file. Required.                        |
| `outputDirectory`| `openapi.outputDir`       | `${project.build.directory}/generated-collections`     | Output directory for the generated collection and environment files.     |
| `format`         | `openapi.format`          | `POSTMAN`                                              | Target collection format. Supported values: `POSTMAN`, `INSOMNIA`.       |
| `collectionName` | `openapi.collectionName`  | API title from the spec                                | Optional collection name override.                                       |
| `baseUrl`        | `openapi.baseUrl`         | First server URL from the spec                         | Optional base URL override for the generated collection.                 |

## Command-line example

```bash
mvn com.github.rspereiratech:openapi-collection-generator-maven-plugin:generate \
    -Dopenapi.spec=src/main/resources/petstore.yaml \
    -Dopenapi.format=INSOMNIA \
    -Dopenapi.outputDir=target/collections
```

## License

See the parent project for license information.