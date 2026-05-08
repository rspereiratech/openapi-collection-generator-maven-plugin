# Output

This document describes what files the plugin writes and how they are named.

## Output directory

By default, files are written to `${project.build.directory}/generated-collections` (typically `target/generated-collections`). Override via the `outputDirectory` parameter or `-Dopenapi.outputDir=...`.

The directory is created if it does not exist.

## Collection file

A single collection file is always written.

- **Path:** `<outputDirectory>/<format>_<collectionName>.json`
- **Format token:** `postman` or `insomnia` (lowercase).
- **Collection name:** taken from the `collectionName` parameter, falling back to the API title from the spec. The name is sanitised — non-alphanumeric characters are replaced with `_`.

Examples (assuming `collectionName=My API`):

| Format     | File written                                   |
|------------|------------------------------------------------|
| `POSTMAN`  | `target/generated-collections/postman_My_API.json`  |
| `INSOMNIA` | `target/generated-collections/insomnia_My_API.json` |

Written by `FileCollectionWriter`.

## Environment files

Environment files are written **only for Postman**. Insomnia embeds environment data inside the main export file, so no extra files are produced.

For Postman, one environment file is generated per server declared in the spec's `servers[]` array (or a single `localhost` environment if no servers are declared).

- **Path:** `<outputDirectory>/<collectionName>_<environmentName>.environment.json`
- **Environment name:** derived from each server's `description`, or one of `Production`/`Staging`/`Development` based on position when no description is present.

Example with two servers (`Production`, `Staging`) and `collectionName=Petstore`:

```
target/generated-collections/
├── postman_Petstore.json
├── Petstore_Production.environment.json
└── Petstore_Staging.environment.json
```

Each environment file contains:

- The server's `baseUrl` as a Postman variable.
- One variable per security scheme detected (e.g. `bearerToken`, `apiKey`, `basicAuthUser`, `basicAuthPass`). Values are left as placeholders for the user to fill in.

Written by `FileEnvironmentWriter` (one call per `AdditionalFile` returned by the generator).

## Stable IDs

All collections, folders, requests, and environments have **deterministic IDs**: a SHA-256 of a context string, truncated to 16 hex characters and prefixed.

| Entity        | Prefix |
|---------------|--------|
| Request       | `req_` |
| Folder        | `fld_` |
| Environment   | `env_` |
| Workspace     | `wrk_` |

This keeps generated files diff-stable across builds: if the spec does not change, the IDs do not change either, which is friendly to git and code review.

See `DeterministicIdGenerator`.
