# AGENT.md (backend)

Stack-specific rules for `backend/` — the Biwenger API client and Lambda
endpoints (Node.js, Serverless Framework). Process-level rules (git
workflow, vertical slicing, backlog, credentials) live in the root
`../AGENT.md` and apply here too.

## Coding

- Naming: `docs/coding-conventions/naming-conventions.md`
- Named parameters: `docs/coding-conventions/named-parameters.md`
- Factory functions and DI: `docs/coding-conventions/factory-functions.md`
- Handler factory pattern: `docs/coding-conventions/handler-factory-pattern.md`
- IIFE singletons: `docs/coding-conventions/iife-singletons.md`
- Persistence at the boundary: `docs/coding-conventions/persistence-boundary.md`
- Share behavior through modeling: `docs/coding-conventions/share-behavior-through-modeling.md`

## Tests

- Testing philosophy: `docs/ways-of-working/testing-strategy.md`
- Refactoring test files: `../docs/ways-of-working/test-refactoring-micro-steps.md` (shared, not backend-specific)

## Documentation

- `docs/adrs/` — Architecture Decision Records (Nygard format); index at `docs/adrs/README.md`
- `docs/concerns/` — backend-specific deferred issues; usage guide at `../docs/ways-of-working/concerns.md`
- `docs/coding-conventions/` — naming, named parameters, factory functions, handler factory pattern, IIFE singletons, persistence boundary, share-behavior-through-modeling
- `requests/` — JetBrains HTTP Client `.rest` files against the raw Biwenger API and our own deployed endpoint; `http-client.env.json` (committed) / `http-client.private.env.json` (gitignored) per directory

Deliberately not ported from interest-tracker: `tell-dont-ask.md` and
`domain-object-modeling.md` (coding conventions), its feature-slice/DDD
architecture docs, and its session-log directory — all tied to a codebase
and domain this project doesn't have yet.
