## What changed

<!-- One or two sentences.  -->

## Why

<!-- Link to issue, ticket, or describe the motivation. -->

## How to verify

<!-- Steps the reviewer can run locally. e.g.
- `docker compose up`
- Hit `POST /api/v1/scores/calculate/<id>`
- Confirm the AVOID badge appears for contractor X
-->

## Checklist

- [ ] Tests added or updated (`contractoriq-api/src/test`)
- [ ] `mvn test` passes locally
- [ ] Frontend builds clean (`npm run build`) if UI touched
- [ ] No secrets committed (`.env` stays in `.gitignore`)
- [ ] README / `docs/` updated if behavior or contract changed
