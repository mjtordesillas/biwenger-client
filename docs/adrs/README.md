# Architecture Decision Records (repo-level)

Decisions about the repository itself — not about `backend/` or
`android/` internals, which have their own `docs/adrs/` under each
subdirectory.

## Format

ADRs use the [Nygard format](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions):
Status, Context, Decision, Consequences.

## Index

| ADR | Title | Status |
|-----|-------|--------|
| [001](./001-monorepo-for-backend-and-android.md) | Monorepo for Backend and Android | Accepted |

## Adding a New ADR

1. Copy the structure from an existing ADR.
2. Number sequentially.
3. Add a row to the index above.
4. Set status to `Proposed` until confirmed; update to `Accepted` when adopted.
