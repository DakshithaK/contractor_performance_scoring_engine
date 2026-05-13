# Security Policy

## Reporting a vulnerability

Don't open a public issue.

Instead, please report security issues privately via GitHub's
[private vulnerability advisories](https://github.com/DakshithaK/contractor_performance_scoring_engine/security/advisories/new),
or email `dakshithakcode@gmail.com` with the subject line
`SECURITY: ContractorIQ`.

You'll get an acknowledgement within 72 hours. Confirmed issues are tracked in
a private advisory; a fix and disclosure timeline are agreed before any public
write-up.

## Scope

In scope:

- The Spring Boot service (`contractoriq-api`)
- The Python vision service (`vision-service`)
- The React frontend (`frontend`)
- The Docker Compose and Kubernetes manifests in this repository

Out of scope:

- Vulnerabilities in upstream dependencies that don't have a working exploit
  against this repo's configuration (Dependabot handles routine upgrades).
- Findings that require physical access, a compromised local machine, or
  social engineering of a maintainer.
- Reports on the demo `.env.example` not enforcing a real `ANTHROPIC_API_KEY`
  format — it's an example file.

## Supported versions

This is a single-branch project (`main`). Only the latest commit on `main`
receives security fixes. Older tagged releases are snapshots only.
