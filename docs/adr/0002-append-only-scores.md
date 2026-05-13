# ADR 0002 — Append-only contractor scores

- **Status:** Accepted
- **Date:** 2026-05-12
- **Deciders:** @DakshithaK

## Context

Every score recalculation produces a new row in `contractor_scores`. The
"current" score for the API DTO is `SELECT ... ORDER BY calculated_at DESC LIMIT 1`.

We could instead `UPSERT` a single row per contractor and keep no history.

## Decision

Keep `contractor_scores` append-only.

## Why

- **Audit trail.** When a contractor's recommendation flips from HIRE to AVOID,
  ops wants to know "what changed?" — having the previous score row makes that
  a one-row diff instead of an investigation.
- **Trending.** The Grafana dashboard plots average overall score over time.
  That needs historical rows.
- **Debugging.** If the scoring math itself changes, we can re-run and compare
  old vs new scores without losing the original.

## Cost

- Table grows monotonically. For a tool that scores ~hundreds of contractors
  nightly, growth is negligible — tens of thousands of rows per year. No
  partitioning needed within a 5-year horizon.
- A future cleanup job can compact rows older than N months to a daily
  snapshot if storage ever matters.

## Indexes

- `idx_cs_contractor`: needed for the per-contractor latest-row read.
- `idx_cs_overall DESC`: leaderboard.
- `idx_cs_recommendation`: flagged-contractors query.

## Revisit if

- Storage cost becomes meaningful (not in any realistic projection).
- A regulatory requirement says we cannot retain prior recommendations.
