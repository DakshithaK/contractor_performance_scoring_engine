# ContractorIQ

[![CI](https://github.com/DakshithaK/contractor_performance_scoring_engine/actions/workflows/ci.yml/badge.svg)](https://github.com/DakshithaK/contractor_performance_scoring_engine/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Postgres](https://img.shields.io/badge/Postgres-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-7.5-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)

Automated contractor performance scoring engine for construction operations.
Ingests project & site data, computes weighted performance scores, and produces a
hire / caution / avoid recommendation backed by Claude-generated reasoning.

> Deeper docs: [architecture diagram and request flows](docs/architecture.md) ·
> [ADRs](docs/adr/) · [contributing](CONTRIBUTING.md) · [changelog](CHANGELOG.md) ·
> [security](SECURITY.md)

## Architecture

- **contractoriq-api** (Spring Boot 3, Java 17, port 8080) — business logic, scoring engine, CRUD, Kafka producer/consumer, Redis cache.
- **vision-service** (FastAPI, Python 3.11, port 8001) — Claude Vision photo analysis and recommendation text generation.
- **postgres** — primary store.
- **redis** — scorecard cache (TTL 1h).
- **kafka + zookeeper** — `score-recalculation-events` event bus.
- **prometheus + grafana** — metrics + dashboards.
- **frontend** (React + Vite) — dashboard, contractor card, photo upload.

## Run locally

```bash
cp .env.example .env   # set ANTHROPIC_API_KEY
cd infrastructure
docker compose --env-file ../.env up --build
```

Frontend (dev):
```bash
cd frontend
npm install
npm run dev
```

## Endpoints (Spring Boot, /api/v1)

| Method | Path | Description |
|--------|------|-------------|
| GET    | /contractors | list contractors with cached scores |
| GET    | /contractors/{id} | full profile + score breakdown |
| POST   | /contractors | create contractor |
| POST   | /projects | log a project — publishes Kafka event |
| POST   | /photos/analyze | upload photo — calls vision-service, publishes event |
| POST   | /scores/calculate/{id} | manual recalculation |
| POST   | /scores/calculate-all | recalculate all contractors |
| GET    | /leaderboard | top 10 by overall_score |
| GET    | /flagged | recommendation = AVOID |
| GET    | /actuator/prometheus | Prometheus metrics |

## Tests

```bash
cd contractoriq-api
mvn test
```

## Monitoring

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin)
