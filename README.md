# ContractorIQ

Automated contractor performance scoring engine for construction operations.
Ingests project & site data, computes weighted performance scores, and produces a
hire / caution / avoid recommendation backed by Claude-generated reasoning.

![ContractorIQ dashboard — contractor performance board](docs/images/dashboard.png)

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

The pre-built Grafana board lives at
`monitoring/grafana/dashboards/contractoriq-dashboard.json` —
import it under *Dashboards → New → Import* after the stack is up.

## Performance

Load-tested locally against the seeded dataset (12 contractors).

| Endpoint                                | Concurrency | RPS   | p50   | p95   | p99   | Failed |
|-----------------------------------------|-------------|-------|-------|-------|-------|--------|
| `GET /contractors/{id}` (Redis cached)  | 25          | 3,063 | 7 ms  | 12 ms | 16 ms | 0      |
| `GET /leaderboard` (DB query)           | 25          | 1,133 | 20 ms | 32 ms | 52 ms | 0      |
| `GET /contractors` (list)               | 10          | 603   | 13 ms | 39 ms | 46 ms | 0      |

Zero failures across 1,200 total requests. The Redis-cached read path
serves ~3× the RPS of the DB-backed `/leaderboard` query — the cache
earns its keep on the dashboard's hot path.

## Notes

- Postgres maps to host port **5433** to avoid clashing with a local Postgres
  on 5432. Containers still talk to it on 5432 internally.
- Without `ANTHROPIC_API_KEY` set, the vision service returns neutral
  placeholder analysis so the rest of the system stays runnable.
