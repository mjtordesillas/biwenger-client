# AGENT.md

## Rules

### Process
- Lean/Agile, trunk-based development. See `docs/ways-of-working/git-workflow.md`.
- Build in tiny, complete vertical slices — never a layer of architecture on its own. See `README.md`.
- Read-only first. No automated bidding/selling without explicit confirmation.
- Never commit credentials. Server-side auth only.
- No abstractions before a second use case demonstrates the need for one — the coding conventions below are ported ahead of the code that will need them; don't force-fit them onto a single handler just to satisfy the doc.

### Coding
- Naming: `docs/coding-conventions/naming-conventions.md`
- Named parameters: `docs/coding-conventions/named-parameters.md`
- Factory functions and DI: `docs/coding-conventions/factory-functions.md`
- Handler factory pattern: `docs/coding-conventions/handler-factory-pattern.md`
- IIFE singletons: `docs/coding-conventions/iife-singletons.md`
- Persistence at the boundary: `docs/coding-conventions/persistence-boundary.md`
- Share behavior through modeling: `docs/coding-conventions/share-behavior-through-modeling.md`

### Tests
- Testing philosophy: `docs/ways-of-working/testing-strategy.md`
- Refactoring test files: `docs/ways-of-working/test-refactoring-micro-steps.md`

## Documentation

- `docs/backlog.md` — candidate features (not a roadmap); pick the next one from actually using the current slice in production
- `docs/rat.md` — the Riskiest Assumption Test: how we authenticate against Biwenger and what the API looks like
- `docs/adrs/` — Architecture Decision Records (Nygard format); index at `docs/adrs/README.md`
- `docs/concerns/` — deferred design/workflow issues; usage guide at `docs/ways-of-working/concerns.md`
- `docs/ways-of-working/` — git workflow, testing strategy, test-refactoring, concerns guide
- `docs/coding-conventions/` — naming, named parameters, factory functions, handler factory pattern, IIFE singletons, persistence boundary, share-behavior-through-modeling

Deliberately not ported from interest-tracker: `tell-dont-ask.md` and
`domain-object-modeling.md` (coding conventions), its feature-slice/DDD
architecture docs, and its session-log directory — all tied to a codebase
and domain this project doesn't have yet.
