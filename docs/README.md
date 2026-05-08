# Documentation

End-user and architectural documentation for the **OpenAPI Collection Generator Maven Plugin**.

## Contents

- [Configuration](configuration.md) — Mojo parameters, properties, and defaults.
- [Architecture](architecture.md) — High-level pipeline and the role of each component.
- [Output](output.md) — Files produced, naming convention, and content.
- [Output Formats](formats.md) — Postman and Insomnia format specifics.
- [OpenAPI Extensions](extensions.md) — Custom `x-*` extensions recognised by the plugin.
- [Security](security.md) — How OpenAPI security schemes are translated into collection auth.

## Quick links

- Plugin goal: `generate` (default phase: `generate-resources`)
- Default spec location: `${project.basedir}/src/main/resources/openapi.yaml`
- Default output directory: `${project.build.directory}/generated-collections`
- Supported formats: `POSTMAN`, `INSOMNIA`
