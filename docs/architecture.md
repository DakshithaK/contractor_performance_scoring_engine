# Architecture

A single picture, then the why behind each box.

```
                  ┌────────────────────┐
                  │  React (Vite)      │  http://localhost:5173
                  │  Dashboard / Card  │
                  └─────────┬──────────┘
                            │ /api/v1 (proxied in dev)
                            ▼
┌────────────────────────────────────────────────────────────┐
│              contractoriq-api · Spring Boot 3.2            │
│                                                            │
│  Controllers ──► Services ──► Repositories ──► Postgres    │
│       │              │             │                       │
│       │              │             └─► JPA / Hibernate     │
│       │              │                                     │
│       │              └─► ScoringEngine (pure logic)        │
│       │                                                    │
│       ├─► ScoreCacheService ──► Redis (1h TTL)             │
│       │                                                    │
│       ├─► ScoreRecalculationProducer ──► Kafka topic       │
│       │                                                    │
│       └─► VisionServiceClient (REST) ──┐                   │
│                                        │                   │
│       ScoreRecalculationConsumer ◄─────┼──── Kafka         │
│            │                           │                   │
│            └─► ScoringEngine.calculate │                   │
│                                        │                   │
│  Micrometer ──► /actuator/prometheus   │                   │
└────────────────────────────────────────┼───────────────────┘
                                         │
                                         ▼
                  ┌────────────────────────────┐
                  │  vision-service · FastAPI  │
                  │                            │
                  │  /analyze-photo            │──► Anthropic
                  │  /generate-recommendation  │    (Opus + Haiku)
                  └────────────────────────────┘

                  ┌──────────────┐   ┌──────────────┐
                  │  Prometheus  │──►│   Grafana    │
                  └──────────────┘   └──────────────┘
                         ▲
                         │ scrapes
                         └── /actuator/prometheus
```

## Request flows

### Read: `GET /api/v1/contractors/{id}`

1. `ContractorController` delegates to `ContractorService`.
2. `ScoreCacheService.get` hits Redis. Cache hit (~1 ms) → return DTO.
3. Cache miss → repository read on `contractor_scores`, build DTO, `put` to Redis with 1 h TTL, return.

### Write: `POST /api/v1/projects`

1. Validated body lands in `ProjectController`.
2. `ProjectService.ingest` persists via JPA.
3. `ScoreRecalculationProducer.publish(contractorId, "PROJECT_ADDED")`.
4. Response returns immediately — the API call is bounded by Postgres + Kafka write latency, **not** by the LLM call.
5. `ScoreRecalculationConsumer` picks up the event, calls `ScoringEngine.calculate`, which talks to the Python sidecar for the reasoning text, persists a new `contractor_scores` row, and invalidates the cache entry.

The same flow runs for `POST /api/v1/photos/analyze` with `PHOTO_ANALYZED`.

## Why the consumer does the heavy work

The LLM call (Anthropic) can take seconds. Doing it inline on the write path would make `POST /projects` feel sluggish and couple our 99-percentile latency to a third-party API. Pushing it onto the consumer means:

- Writes return fast.
- Failed LLM calls don't fail the project save.
- Adding a second consumer instance horizontally scales reasoning generation.
- We get an idempotent retry point if we ever wire one up.

## Why two services

See [`adr/0001-two-service-split.md`](adr/0001-two-service-split.md).

## Data model

Four tables, all UUID-keyed, with `gen_random_uuid()` defaults from `pgcrypto`:

| Table                 | Notes                                                                                       |
|-----------------------|---------------------------------------------------------------------------------------------|
| `contractors`         | Profile. `trade` is an enum check (CIVIL/ELECTRICAL/PLUMBING/FINISHING).                    |
| `projects`            | One row per project. `completion_status` enum, `customer_rating` 1–5 check.                 |
| `site_photo_analyses` | One row per analyzed photo. `claude_raw_response` and `issues_found` are JSONB.             |
| `contractor_scores`   | Append-only log of score recalculations. Latest row wins for the API DTO.                   |

`contractor_scores` is append-only on purpose — keeping the history lets us
trend a contractor's overall score over time on the Grafana dashboard, and
gives us a forensic trail if a recommendation flips unexpectedly.

## Observability

- **Metrics:** Micrometer → Prometheus → Grafana. Pre-built dashboard at [`monitoring/grafana/dashboards/contractoriq-dashboard.json`](../monitoring/grafana/dashboards/contractoriq-dashboard.json).
- **Logs:** Spring Boot default JSON via Logback. Request and consumer events log a contractor UUID for grep-ability.
- **Health:** `/actuator/health/{liveness,readiness}` separated for Kubernetes probes.

## Failure modes worth knowing

| Failure                              | Behavior                                                    |
|--------------------------------------|-------------------------------------------------------------|
| Anthropic API down                   | `VisionServiceClient` throws; reasoning falls back to a fixed string; score still calculates. |
| Vision service down                  | Same as above; the recommendation tier is unaffected (it's deterministic, not LLM-derived).   |
| Redis down                           | Cache layer catches and logs; calls hit Postgres directly. Throughput halves, app stays up.   |
| Kafka down                           | Project / photo writes still succeed; events are dropped (no DLQ today — see ADR backlog).    |
| Postgres down                        | API returns 5xx. Nothing graceful — this is a hard dependency.                                |

## Open work

- Wire a DLQ for `ScoreRecalculationConsumer` so events aren't dropped on transient failures.
- Add request-correlation IDs end-to-end (Spring filter → Python service).
- Replace the synchronous Anthropic call inside the consumer with a small retry/backoff wrapper.
- Add a `V2__` migration once a fifth scoring dimension lands.
