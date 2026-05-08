# Security Policy

## Supported versions

This project is in active early development. Only the latest released version on `master` receives security updates.

| Version          | Supported          |
|------------------|--------------------|
| `1.0.0-SNAPSHOT` | :white_check_mark: |
| Older snapshots  | :x:                |

## Reporting a vulnerability

If you believe you have found a security vulnerability in this plugin, **please do not open a public GitHub issue**.

Instead, report it privately by either:

- Using GitHub's [private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability) on this repository, or
- Sending an email to **rspereiratech@gmail.com** with the subject `[security] openapi-collection-generator-maven-plugin`.

Please include:

- A clear description of the issue and its impact.
- Steps to reproduce, ideally with a minimal OpenAPI spec and plugin configuration.
- The plugin version, Java version, and Maven version.
- Any suggested mitigation, if you have one.

You should expect an initial response within **5 business days**. If the issue is confirmed, a fix will be coordinated and a security advisory will be published once a patched version is available.

## Scope

In scope:

- Vulnerabilities in the plugin code that affect users running the plugin against trusted or untrusted OpenAPI specifications (e.g. arbitrary file write outside `outputDirectory`, code execution during spec parsing, exfiltration of secrets via generated collections).
- Issues in how secrets and environment variables are emitted in generated Postman/Insomnia files.

Out of scope:

- Vulnerabilities in third-party dependencies (`swagger-parser`, `jackson`, the Maven plugin API). Please report those upstream. We will, however, bump dependencies once an upstream fix is available.
- Issues that require running the plugin with an attacker-controlled `pom.xml` or attacker-controlled `~/.m2` — at that point the build environment is already compromised.

## Disclosure policy

We follow coordinated disclosure. Once a fix is released, a GitHub Security Advisory will be published describing the vulnerability, affected versions, and the fixed version. Credit will be given to the reporter unless they request otherwise.
