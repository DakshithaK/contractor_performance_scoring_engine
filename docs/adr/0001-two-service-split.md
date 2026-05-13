# ADR 0001 — Split Claude integration into a Python sidecar

- **Status:** Accepted
- **Date:** 2026-05-12
- **Deciders:** @DakshithaK

## Context

The project needs to call Anthropic's Claude API for two purposes:

1. Vision analysis of construction site photos (`claude-opus-4-6`).
2. Generating the natural-language hire/avoid reasoning shown on the contractor
   card (`claude-haiku-4-5`).

The rest of the system — business logic, scoring, persistence, Kafka, the API
surface — is a Spring Boot service. We have to decide where the Anthropic
integration lives.

## Options considered

### A. Keep everything in Java (single service)

Use Anthropic's Java SDK from inside the Spring service. No second deployable.

**Pros:** Fewer moving parts. One process to deploy, monitor, and scale.

**Cons:** Anthropic's Java SDK is a second-class citizen compared to Python.
Vision API ergonomics (base64 encoding, multipart content blocks, JSON
extraction) are noticeably more painful in Java. Tests of LLM-adjacent code
end up doing a lot of byte-array gymnastics.

### B. Split: Java service + thin Python sidecar (chosen)

Spring Boot stays the "system of record." A small FastAPI service exposes two
endpoints (`/analyze-photo`, `/generate-recommendation`) and the Spring side
calls them via `RestTemplate`.

**Pros:**
- Python's `anthropic` SDK is the reference implementation; vision messages
  are a 4-line snippet.
- The sidecar has zero state — easy to redeploy, kill, replace, or swap with
  a different model provider entirely without touching the Java code.
- The contract between services is REST + JSON, which gives us a clean test
  seam (we mock the HTTP boundary, not the Anthropic SDK).
- Scaling characteristics differ between the two workloads. The Python side
  is IO-bound on Anthropic; the Java side is CPU/DB bound on scoring. Putting
  them in separate pods lets us scale each independently.

**Cons:**
- One more service to operate.
- Cross-language deployments — needs Docker Compose locally and two image
  builds in CI.

### C. Run Claude calls in-process via a serverless function

Use AWS Lambda / Cloud Functions for the LLM call. Spring talks to it over
HTTPS.

**Pros:** Even cleaner separation; pay-per-call.

**Cons:** Adds cloud lock-in to a project that explicitly avoids cloud
deployment config. Cold starts hurt the consumer path. Local development gets
materially harder.

## Decision

**Option B.** The cost (a small extra deployable) is bought back many times
over by code clarity and the freedom to swap models or providers in one
service without coordinating two.

## Consequences

- The repository carries two Dockerfiles, two `requirements.txt`/`pom.xml`,
  and two CI jobs.
- Local bring-up requires Docker Compose, not just a Spring `mvn spring-boot:run`.
- The Spring side needs a resilient HTTP client (timeouts, fallbacks) for the
  vision service — handled in `VisionServiceClient`.
- Adding a new LLM-driven feature means deciding "does this belong in the
  sidecar or in Spring?" The default is the sidecar.

## Revisit if

- We end up reaching for the Anthropic Java SDK for an unrelated reason and
  already have it on the classpath.
- A single deployable becomes a hard constraint (e.g., a customer-managed
  on-prem build where extra services aren't acceptable).
