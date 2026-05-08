# Output Formats

The plugin currently targets two formats: **Postman** and **Insomnia**. Both share the same OpenAPI parsing and enrichment pipeline; only the final layout and serialisation differ.

## Postman

- **Schema:** Postman Collection v2.1.0 (`https://schema.getpostman.com/json/collection/v2.1.0/collection.json`).
- **Generator:** `PostmanCollectionGenerator`.
- **Grouping:** operations are grouped into folders by their first tag (`TagOperationGrouper`). Untagged operations land in a `default` folder. Callback operations are grouped under `Callbacks`.
- **Environment:** emitted as **separate files** (`*.environment.json`), one per server. See [Output](output.md).
- **Auth variables:** rendered as Postman variables (`{{variableName}}`) in headers and query parameters, with values left as placeholders inside the environment file.
- **Deprecated operations:** marked by `PostmanDeprecationMarker` — the request name is prefixed with `[DEPRECATED] `, and the description is prepended with `⚠ This operation is deprecated.`.

### Postman models

Located under `plugin.postman.model`:

- `PostmanCollection`, `PostmanInfo`, `PostmanItem` — top-level structure.
- `PostmanRequest`, `PostmanUrl`, `PostmanHeader`, `PostmanQueryParam` — request-level details.
- `PostmanBody`, `PostmanBodyRaw`, `PostmanBodyOptions` — request body (raw JSON / form / urlencoded).
- `PostmanVariable` — environment variable.

## Insomnia

- **Format:** Insomnia v4 export (`InsomniaExport`).
- **Generator:** `InsomniaCollectionGenerator`.
- **Grouping:** workspace + tag-based `InsomniaRequestGroup` folders, plus a `Callbacks` folder for callback operations.
- **Environment:** **embedded** in the export file as `InsomniaEnvironment` resources, one per server. No separate environment files are written.
- **Auth variables:** Insomnia template variables (e.g. `{{ token }}`).
- **Deprecated operations:** marked by `InsomniaDeprecationMarker` — the name is prefixed with `⚠ ` and suffixed with ` (deprecated)`. The description starts with `DEPRECATED: This operation may be removed in a future version.` (plain text, no Markdown).

### Insomnia models

Located under `plugin.insomnia.model`:

- `InsomniaExport`, `InsomniaWorkspace`, `InsomniaResource` — top-level structure.
- `InsomniaRequest`, `InsomniaRequestGroup`, `InsomniaParameter`, `InsomniaHeader` — request-level details.
- `InsomniaBody` — request body.
- `InsomniaEnvironment` — embedded environment resource (one per server).

## What is shared

Both generators run the same pre-processing:

- Schema example generation (`core/example/`) for request bodies and parameters.
- Schema `$ref` and discriminator resolution (`core/schema/`).
- Extension processors for `x-beta`, `x-deprecated-since`, `x-internal`, `x-summary` (see [Extensions](extensions.md)).
- Security injection from `securitySchemes` (see [Security](security.md)).
- Callback flattening (`core/callback/`).
- Link enrichment in operation descriptions (`core/link/`).
- Deterministic ID generation (`core/id/`).
