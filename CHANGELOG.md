# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Project quality scaffolding: LICENSE (MIT), Dependabot, EditorConfig,
  PR/Issue templates, SECURITY policy, CONTRIBUTING guide, CHANGELOG.
- GitHub Actions CI workflow covering Spring Boot tests, vision-service
  install, and Vite build.
- JaCoCo coverage report on the Java side.
- Architecture documentation at `docs/architecture.md`.

## [0.1.0] - 2026-05-13

### Added

- Initial release.
- Spring Boot 3.2 API with the four-dimension scoring engine
  (delay 25%, budget 25%, quality 30%, customer 20%).
- Python FastAPI sidecar for Claude Vision photo analysis and recommendation
  text generation.
- Postgres schema via Flyway, Redis cache layer for scorecards,
  Kafka topic `score-recalculation-events` with producer + consumer.
- React + Vite frontend: dashboard, contractor card, photo upload, project form.
- Docker Compose for local development; Kubernetes manifests under
  `infrastructure/k8s/`.
- Prometheus scrape config + pre-built Grafana dashboard.
- Postman collection covering every endpoint.
- Demo data seeder loading 12 contractors across 4 cities.
- JUnit 5 + Mockito tests for the scoring engine, contractor service cache
  behavior, and vision service client error paths.
