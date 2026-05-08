# OpenAPI Extensions

The plugin recognises a small set of OpenAPI vendor extensions (`x-*`) at the operation level and uses them to enrich the generated request name and description. Extensions are processed by an `ExtensionProcessorChain` — every applicable processor runs in sequence.

## Supported extensions

| Extension            | Type     | Effect on name                              | Effect on description                                          |
|----------------------|----------|---------------------------------------------|----------------------------------------------------------------|
| `x-beta`             | boolean  | Appends ` (Beta)` to the request name.      | Prepends `Beta — may change without notice.`                   |
| `x-internal`         | boolean  | Appends ` (Internal)` to the request name.  | Prepends `Internal endpoint.`                                  |
| `x-deprecated-since` | string   | —                                           | Appends `Deprecated since: **{version}**`.                     |
| `x-summary`          | string   | Replaces the request name with this value.  | —                                                              |

When multiple extensions apply to the same operation, all relevant processors run. Name overrides cascade (last wins); description fragments are concatenated.

## Examples

### `x-beta`

```yaml
paths:
  /users:
    post:
      summary: Create user
      x-beta: true
```

Resulting request:

- **Name:** `Create user (Beta)`
- **Description:** starts with `Beta — may change without notice.`

### `x-internal`

```yaml
paths:
  /admin/flush:
    post:
      summary: Flush caches
      x-internal: true
```

- **Name:** `Flush caches (Internal)`
- **Description:** starts with `Internal endpoint.`

### `x-deprecated-since`

```yaml
paths:
  /v1/widgets:
    get:
      summary: List widgets
      x-deprecated-since: "2.0.0"
```

- **Description:** ends with `Deprecated since: **2.0.0**`.

Note: this is independent of OpenAPI's own `deprecated: true` flag, which triggers the format-specific deprecation marker (see [Output Formats](formats.md)).

### `x-summary`

```yaml
paths:
  /users/{id}:
    get:
      summary: getUserById
      x-summary: Get user by ID
```

- **Name:** `Get user by ID` (instead of the operation's `summary`).

Use `x-summary` when you want a human-readable name in collections without changing the canonical `summary` (which often doubles as a code-generation operation ID).

## Implementation

Each extension is implemented as a small `ExtensionProcessor` under `core/extension/impl/`:

- `XBetaExtensionProcessor`
- `XInternalExtensionProcessor`
- `XDeprecatedSinceExtensionProcessor`
- `XSummaryExtensionProcessor`

To add support for a new extension, implement `ExtensionProcessor` and register the new processor in the chain.
