# AGENT.md

This is a monorepo: `backend/` (this repo's original content — the
Biwenger API client and Lambda endpoints) and `android/` (the native
Android client, imported with history from the former
`biwenger-client-android` repo). Rules here apply to both; each
subdirectory has its own `AGENT.md` for stack-specific conventions —
`backend/AGENT.md` for JS/Lambda, `android/AGENT.md` for Kotlin/Compose.

## Rules

### Process
- Lean/Agile, trunk-based development. See `docs/ways-of-working/git-workflow.md`.
- Deliver whatever feature is chosen next as the thinnest possible complete vertical slice (Elephant Carpaccio). See `docs/ways-of-working/vertical-slicing.md`. Feature selection itself is not this rule's concern — see `docs/backlog/` (organized by state — `docs/ways-of-working/backlog.md`). A slice will often need both `backend/` and `android/` work; that's still one slice, one backlog file — not two.
- Read-only first. No automated bidding/selling without explicit confirmation.
- Never commit credentials. Server-side auth only for the backend; the Android app holds only the API key needed to call it, never Biwenger credentials directly (see `android/AGENT.md`'s Secrets convention).
- No abstractions before a second use case demonstrates the need for one — the coding conventions below (and each stack's own) are ported ahead of the code that will need them; don't force-fit them onto a single handler/screen just to satisfy the doc.

## Documentation

- `docs/backlog/{to-do,in-progress,done}/` — one file per candidate feature, organized by state (not a roadmap), covering both stacks where a feature touches both; usage guide at `docs/ways-of-working/backlog.md`
- `docs/rat.md` — the Riskiest Assumption Test: how we authenticate against Biwenger and what the API looks like
- `docs/biwenger-api-notes.md` — ongoing Biwenger API discoveries made outside the original RAT, relevant to both stacks; explorable via `backend/requests/` (JetBrains HTTP Client)
- `docs/ways-of-working/` — git workflow, vertical slicing, backlog, concerns guide, test-refactoring-in-micro-steps (stack-agnostic technique)
- `docs/concerns/` — deferred design/workflow issues that span both stacks or the repo itself; usage guide at `docs/ways-of-working/concerns.md`. Stack-specific concerns live under `backend/docs/concerns/`.
- Stack-specific ADRs and coding conventions live under `backend/docs/` and `android/docs/` respectively — see each subdirectory's own `AGENT.md`.

### Deploy

- `backend/` deploys continuously: push to `main` with changes under `backend/**` runs CI (test, then `serverless deploy`) via `.github/workflows/backend-ci.yaml`.
- `android/` deploys manually: this is a personal app, installed via USB with `make install` (build, test, `adb install`) from `android/`. No CI for it — not worth the setup for a single-device manual install.
