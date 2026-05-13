# ContractorIQ

Automated contractor performance scoring engine for construction operations.
Ingests project & site data, computes weighted performance scores, and produces a
hire / caution / avoid recommendation backed by Claude-generated reasoning.

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
