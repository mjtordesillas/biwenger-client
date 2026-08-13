# AGENT.md

## Rules

- Lean/Agile, trunk-based development. See `docs/ways-of-working/git-workflow.md`.
- Build in tiny, complete vertical slices — never a layer of architecture on its own. See `README.md`.
- Read-only first. No automated bidding/selling without explicit confirmation.
- Never commit credentials. Server-side auth only.
- No abstractions before a second use case demonstrates the need for one.

## Documentation

- `docs/rat.md` — the Riskiest Assumption Test: how we authenticate against Biwenger and what the API looks like
- `docs/adrs/` — Architecture Decision Records (Nygard format); index at `docs/adrs/README.md`
- `docs/concerns/` — deferred design/workflow issues; usage guide at `docs/ways-of-working/concerns.md`
- `docs/ways-of-working/git-workflow.md` — branching/commit conventions

This file stays short deliberately. Add sections (coding conventions,
testing strategy, architecture) only once there's real code and a second
use case to generalize from — not in advance.
