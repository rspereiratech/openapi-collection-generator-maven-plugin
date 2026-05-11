# Contributing

Thanks for your interest in contributing! This document describes how to set up the project locally and the guidelines for submitting changes.

## Prerequisites

- Java 17+
- Maven 3.9+
- Git

## Project layout

This module is part of the `openapi-collection-generator` family of artifacts. It inherits from the sibling `openapi-collection-generator-parent` POM, which centralises dependency versions, plugin versions, and shared properties.

When working locally, the parent must be available either:

- as a sibling directory (`../openapi-collection-generator-parent`), resolved via the `<relativePath>` in this module's POM, or
- already installed in your local Maven repository (`mvn install` from the parent project).

## Build

```bash
mvn clean verify
```

## Run the plugin against your own spec

```bash
mvn io.github.rspereiratech:openapi-collection-generator-maven-plugin:generate \
    -Dopenapi.spec=path/to/openapi.yaml \
    -Dopenapi.format=POSTMAN \
    -Dopenapi.outputDir=target/collections
```

See [docs/configuration.md](docs/configuration.md) for the full parameter list.

## Coding guidelines

- Target Java 17. Prefer `record`s for immutable data and `sealed` types where helpful.
- Keep the package layout aligned with the existing structure (see [docs/architecture.md](docs/architecture.md)).
- New behaviour goes behind an interface in `core/` whenever possible; format-specific code lives under `postman/` or `insomnia/`.
- Follow the existing chain-of-responsibility patterns for extension processors, schema example generators, and security injectors. Adding a new `x-*` extension or auth scheme should not require modifying existing classes — only adding a new one and registering it.
- Avoid adding heavyweight dependencies. The plugin currently relies on `swagger-parser`, `jackson`, and the Maven plugin API.

## Tests

- Unit tests use JUnit 5 (`org.junit.jupiter.api.Assertions`) and Mockito, declared in the parent's `dependencyManagement`. Do not introduce AssertJ.
- The `GenerateCollectionMojo` exposes a package-private constructor specifically for injecting test doubles — prefer that over reflection.
- Aim for small, focused unit tests around new components. Generators (Postman/Insomnia) should be covered by tests that load a small representative spec from `src/test/resources/`.

Run tests with:

```bash
mvn test
```

## Submitting changes

1. Fork the repository and create a feature branch from `master`.
2. Keep commits focused. Use clear, imperative commit subjects (e.g. `add x-rate-limit extension processor`).
3. Update the relevant docs under `docs/` when you change user-visible behaviour or add a new component.
4. Open a pull request describing the motivation and the change. Reference any related issues.
5. Make sure CI is green before requesting review.

## Reporting issues

For bug reports and feature requests, open an issue on the [GitHub repository](https://github.com/rspereiratech/openapi-collection-generator-maven-plugin/issues). Include:

- The OpenAPI spec snippet that triggers the issue (when relevant).
- The plugin configuration in use.
- The expected vs. actual output.
- Plugin version, Java version, and Maven version.

For security-sensitive reports, see [SECURITY.md](SECURITY.md) instead.
