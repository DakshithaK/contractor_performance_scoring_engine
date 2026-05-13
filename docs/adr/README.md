# Architecture Decision Records

Short, dated notes on choices that would be hard to reverse later, written
when the decision is fresh. We use the [Michael Nygard format](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

| ID   | Title                                                          | Status   |
|------|----------------------------------------------------------------|----------|
| 0001 | [Split Claude integration into a Python sidecar](0001-two-service-split.md) | Accepted |
| 0002 | [Append-only contractor scores](0002-append-only-scores.md)    | Accepted |

## When to write an ADR

If you find yourself in a PR review explaining "we considered X but went with Y
because…", that explanation belongs in an ADR, not in a PR comment.

## When not to

Routine code-level decisions (which library, which folder layout). ADRs are for
the choices a new engineer will trip over six months from now if they don't
know the history.
