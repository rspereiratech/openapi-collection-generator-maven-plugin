# Architecture

The plugin follows a small, layered pipeline: **load → parse → enrich → generate → write**. All stages are abstracted behind interfaces to keep the Mojo testable and the generators interchangeable.

## High-level pipeline

```
specFile ──▶ SpecLoader ──▶ OpenApiParser ──▶ CollectionGenerator ──▶ CollectionWriter
                                                  │
                                                  └──▶ generateAdditionalFiles ──▶ EnvironmentWriter
```

The Mojo (`GenerateCollectionMojo#execute`) wires these stages together. It builds an immutable `PluginConfig` from the user parameters, converts it to a `GenerationConfig`, picks a generator via `CollectionGeneratorFactory`, and dispatches to writers.

## Component overview

### Entry point

- `GenerateCollectionMojo` — Maven goal `generate`. Holds the `@Parameter`-annotated fields, instantiates default collaborators, and runs the pipeline.
- `PluginConfig` (record) — immutable carrier of user parameters; converts to `GenerationConfig` for the core layer.
- `GenerationConfig` (record) — config consumed by generators (output dir, format, collection name).

### Loading and parsing

- `FileSpecLoader` (`SpecLoader`) — validates that the spec file exists and is a regular file.
- `SwaggerOpenApiParser` (`OpenApiParser`) — wraps `io.swagger.parser.v3:swagger-parser`, with `resolve=true` and `resolveFully=true`, returning a fully resolved `OpenAPI` model. Throws `OpenApiParseException` on failure.

### Generation

- `CollectionGenerator` — interface with two methods:
    - `String generate(OpenAPI, GenerationConfig)` — the main collection JSON.
    - `List<AdditionalFile> generateAdditionalFiles(OpenAPI, GenerationConfig)` — environment files.
- `CollectionGeneratorFactory` — picks the right generator for a given `CollectionFormat`.
- `DefaultCollectionGeneratorFactory` — the production wiring for `POSTMAN` and `INSOMNIA` generators.
- `PostmanCollectionGenerator` — produces a Postman Collection v2.1.0 document, grouping operations by their first tag (see `TagOperationGrouper`). Emits one or more environment files (one per server) plus auth-related variables.
- `InsomniaCollectionGenerator` — produces an `InsomniaExport` containing a workspace, environments (one per server), and tag-based request groups. Environments are embedded in the export, not written as separate files.

### Cross-cutting enrichment

These services run inside the generators to add metadata to operations:

- **Schema example generation** (`core/example/`) — chain-of-responsibility (`Array`, `Composed`, `Nullable`, `Object`, `Primitive`, `Delegating`) that produces example bodies and parameters from JSON schemas. `DelegatingSchemaExampleGenerator` breaks circular dependencies between generators.
- **Schema reference resolution** (`core/schema/`) — `DefaultSchemaRefResolver` resolves `$ref` strings against `components.schemas`; `DefaultDiscriminatorResolver` picks the right concrete schema for `oneOf`/`anyOf` with discriminators.
- **Extension processors** (`core/extension/`) — chained via `ExtensionProcessorChain`. Recognise `x-beta`, `x-deprecated-since`, `x-internal`, `x-summary`. See [Extensions](extensions.md).
- **Security application** (`core/security/`) — translates OpenAPI `securitySchemes` and per-operation `security` requirements into headers, query parameters, and environment variables. See [Security](security.md).
- **Server environments** (`core/server/`) — `DefaultServerEnvironmentGenerator` derives one environment per `servers[]` entry, falling back to `localhost` if no servers are declared.
- **Callbacks** (`core/callback/`) — `DefaultCallbackProcessor` flattens OpenAPI callbacks into top-level paths under a "Callbacks" group.
- **Link enrichment** (`core/link/`) — `DefaultLinkDescriptionEnricher` appends a "Related Operations" Markdown section to operation descriptions, sourced from `links`.
- **Deprecation markers** — format-specific (`PostmanDeprecationMarker`, `InsomniaDeprecationMarker`); see [Output Formats](formats.md).

### Identifiers

- `IdGenerator` — interface for stable IDs.
- `DeterministicIdGenerator` — SHA-256 of a context string, truncated to 16 hex chars, prefixed (e.g. `req_`, `fld_`, `env_`, `wrk_`). The same input always produces the same ID, which keeps generated files diff-stable across builds.

### Serialization and writing

- `CollectionSerializer` / `JacksonCollectionSerializer` — turns generator models into JSON via Jackson.
- `FileCollectionWriter` (`CollectionWriter`) — writes the main collection JSON to `outputDirectory`.
- `FileEnvironmentWriter` (`EnvironmentWriter`) — writes each `AdditionalFile` (typically Postman environment files) to `outputDirectory`.

See [Output](output.md) for filename conventions.

## Module layout

```
plugin/
├── GenerateCollectionMojo          # Mojo entry point
├── config/                          # PluginConfig
├── factory/                         # DefaultCollectionGeneratorFactory
├── core/                            # Format-agnostic building blocks
│   ├── callback/  config/  deprecated/  example/  extension/
│   ├── factory/   generator/  id/   link/   loader/   model/
│   ├── parser/    schema/  security/  serializer/  server/  writer/
├── postman/                         # Postman-specific generator + models
└── insomnia/                        # Insomnia-specific generator + models
```

The split between `core/` and the format-specific packages keeps the Postman and Insomnia generators independent: each has its own builders, models, and deprecation marker, but reuses everything under `core/` (schema examples, security, extensions, callbacks, etc.).

## Dependency injection

The Mojo has two constructors:

- A **default constructor** that wires the production implementations (`FileSpecLoader`, `SwaggerOpenApiParser`, `DefaultCollectionGeneratorFactory`, `FileCollectionWriter`, `FileEnvironmentWriter`).
- A **package-private constructor** that accepts all collaborators as arguments — used by unit tests to inject fakes/mocks.

This keeps the Mojo trivially testable without requiring a DI framework.
