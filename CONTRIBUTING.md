# Contributing

This is a small project but the conventions matter. A 5-minute read before
your first PR saves a back-and-forth.

## Local setup

You need:

- JDK 17 (Temurin recommended), Maven 3.9+
- Python 3.11+
- Node 20+
- Docker + Docker Compose

See `README.md` for the one-command bring-up.

## Running tests

```bash
# Java
cd contractoriq-api && mvn test

# Python (smoke)
cd vision-service && python -c "import main; import services.vision_analyzer; import services.recommendation"

# Frontend build
cd frontend && npm ci && npm run build
```

CI runs all three on every PR.

## Branches

- `main` is protected. PRs only.
- Branch name: `feat/<short-slug>`, `fix/<short-slug>`, `docs/<short-slug>`.
- Keep PRs small. If a PR has more than ~400 changed lines (excluding generated
  files), consider splitting.

## Commits

Conventional Commits, kept loose:

```
<type>: <short summary>

<optional body explaining *why*>
```

Where `<type>` is one of: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`,
`perf`, `ci`. The summary stays under 70 characters.

## Code style

- Java: 4-space indent, no Lombok (per project decision — see ADR). Public
  methods have explicit return types and javadoc only when the contract isn't
  obvious from the name.
- Python: 4-space indent, type hints on function signatures.
- JS/JSX: 2-space indent, functional React only, no class components.
- All files: LF line endings, UTF-8 (enforced by `.editorconfig`).

## Scoring engine changes

Any change to `ScoringEngine` requires:

1. A new test case in `ScoringEngineTest`.
2. The relevant weight / threshold in the README scoring table.

The scoring math is the only piece of business logic in this repo. Treat it
with care.

## Adding a dimension

If you're adding a fifth scoring dimension:

1. Add the column in `V2__add_<dimension>.sql` (don't edit V1).
2. Add the field on `ContractorScore`, `ContractorScoreDTO`, and the DB row.
3. Add the calculation method in `ScoringEngine`.
4. Adjust weights so they sum to 1.0. Document why you re-balanced.
5. Update the React `ScoreBreakdown` component.
6. Cover the new dimension with tests.

## Reviewing

- One approval required.
- Reviewer checks: does it have tests, does CI pass, is the change scope right.
- Style nits go in suggested-changes blocks, not blocking comments.
