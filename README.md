# OpenAPI Collection Generator Maven Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Maven Plugin API](https://img.shields.io/badge/Maven%20Plugin%20API-3.9-C71A36.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/ref/3.9.6/maven-plugin-api/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3-6BA539.svg?logo=openapiinitiative&logoColor=white)](https://www.openapis.org/)
[![swagger-parser](https://img.shields.io/badge/swagger--parser-2.1-85EA2D.svg?logo=swagger&logoColor=black)](https://github.com/swagger-api/swagger-parser)
[![Postman](https://img.shields.io/badge/Postman-v2.1-FF6C37.svg?logo=postman&logoColor=white)](https://schema.postman.com/)
[![Insomnia](https://img.shields.io/badge/Insomnia-v4-4000BF.svg?logo=insomnia&logoColor=white)](https://insomnia.rest/)
[![Status: Snapshot](https://img.shields.io/badge/status-snapshot-orange.svg)](#)

Maven plugin that generates **Postman** and **Insomnia** collections from an OpenAPI specification, with support for security schemes, custom `x-*` extensions, callbacks, and per-server environment files.

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

See [docs/configuration.md](docs/configuration.md) for full details.

## Command-line example

```bash
mvn com.github.rspereiratech:openapi-collection-generator-maven-plugin:generate \
    -Dopenapi.spec=src/main/resources/petstore.yaml \
    -Dopenapi.format=INSOMNIA \
    -Dopenapi.outputDir=target/collections
```

## Features

- **Postman v2.1.0** and **Insomnia v4** output formats.
- Tag-based folder grouping.
- Per-server **environment files** (Postman) or embedded environments (Insomnia).
- Built-in support for OpenAPI **security schemes**: HTTP Bearer/Basic, API key (header/query/cookie), and OAuth2 — see [docs/security.md](docs/security.md).
- Custom **`x-*` extensions** for marking operations as Beta, Internal, or deprecated since a given version — see [docs/extensions.md](docs/extensions.md).
- **Callbacks** flattened into a dedicated folder.
- **Deterministic IDs** for diff-stable, code-review-friendly output.

## Documentation

Full documentation lives in the [`docs/`](docs/) folder:

- [Configuration](docs/configuration.md)
- [Architecture](docs/architecture.md)
- [Output](docs/output.md)
- [Output Formats (Postman / Insomnia)](docs/formats.md)
- [OpenAPI Extensions](docs/extensions.md)
- [Security](docs/security.md)

## Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, coding guidelines, and the pull-request workflow.

## Security

To report a security issue, please follow the process described in [SECURITY.md](SECURITY.md). Do **not** open a public issue.

## License

This project is licensed under the [MIT License](LICENSE).
