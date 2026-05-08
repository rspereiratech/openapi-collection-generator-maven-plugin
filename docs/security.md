# Security

The plugin translates OpenAPI `securitySchemes` and per-operation `security` requirements into the target collection's auth model: headers, query parameters, and environment variables.

## Pipeline

1. The operation's first `security` requirement is resolved by `DefaultSecuritySchemeResolver` against `components.securitySchemes`.
2. `DefaultSecurityInjectorFactory` returns the matching `SecurityInjector` for the resolved scheme (first match wins; falls back to `NoOpSecurityInjector` if nothing matches).
3. The injector returns a `SecurityInjection` record carrying:
   - `List<HttpHeader>` — headers to add to the request.
   - `List<HttpQueryParam>` — query parameters to add to the request.
   - `List<EnvironmentVariable>` — variables to declare in the environment file (Postman) or workspace environment (Insomnia).
4. `DefaultSecurityApplier` merges these into the request being built.

## Supported schemes

| OpenAPI scheme                                | Injector                          | Adds to request                                              | Environment variable(s)              |
|-----------------------------------------------|-----------------------------------|--------------------------------------------------------------|--------------------------------------|
| `http` / `bearer`                             | `BearerSecurityInjector`          | Header `Authorization: Bearer {{bearerToken}}`               | `bearerToken`                        |
| `http` / `basic`                              | `BasicAuthSecurityInjector`       | Header `Authorization: Basic {{basicAuthUser}}:{{basicAuthPass}}` | `basicAuthUser`, `basicAuthPass`     |
| `apiKey` / `header`                           | `ApiKeyHeaderSecurityInjector`    | Header `<scheme.name>: {{apiKey}}`                           | `apiKey`                             |
| `apiKey` / `query`                            | `ApiKeyQuerySecurityInjector`     | Query param `<scheme.name>={{apiKey}}`                       | `apiKey`                             |
| `apiKey` / `cookie`                           | `ApiKeyCookieSecurityInjector`    | Cookie/header equivalent for `<scheme.name>`                 | `apiKey`                             |
| `oauth2` (any flow)                           | `OAuth2SecurityInjector`          | Header `Authorization: Bearer {{oauth2Token}}`               | `oauth2Token`                        |
| anything else                                 | `NoOpSecurityInjector`            | nothing                                                      | none                                 |

## Operation-level vs. global security

OpenAPI lets you declare `security` at both the document root and per-operation level. The plugin uses the **operation-level** `security` requirement when present, and falls back to the global one otherwise. An empty `security: []` on an operation disables security for that operation (per the OpenAPI spec).

Only the **first** security requirement is resolved (multiple alternative requirements are not combined).

## Where the variables end up

- **Postman** — variables are placed in **per-server environment files** (`*.environment.json`). Open the environment in Postman, fill in the values, and select it in the runner.
- **Insomnia** — variables are embedded in `InsomniaEnvironment` resources inside the export file, one per server. Activate the appropriate environment in Insomnia and fill in the values.

In both cases values are written as placeholders by the plugin; they are never sourced from the OpenAPI spec, the build, or any environment.

## Extending

To add support for a new auth mechanism:

1. Implement `SecurityInjector` (returning a `SecurityInjection`).
2. Register the injector in `DefaultSecurityInjectorFactory` ahead of `NoOpSecurityInjector`.
3. The order in the factory determines precedence — the first injector whose `supports(scheme)` returns true is used.
