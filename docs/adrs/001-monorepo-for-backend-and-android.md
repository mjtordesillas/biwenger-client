# 001. Monorepo for Backend and Android

## Status

Accepted (2026-08-17)

## Context

`biwenger-client` (backend, Node/Serverless) and `biwenger-client-android`
(native Android client) lived in two separate repos. That was fine while
most slices were single-stack. It stopped being fine once the project's
actual pattern became clear: **almost every slice touches both** — a new
view means a new backend endpoint *and* the Android screen/composable
that renders it. `docs/backlog/done/view-match-day-details.md` shows the
cost this was already imposing: a backend-side backlog file having to
narrate the Android side's progress by commit hash
(`biwenger-client-android` commit `0314fb0`, `82c7f51`, ...) because
there was no shared place to just write it down as it happened.

Explicitly not the goal: atomic cross-repo commits. The two stacks have
genuinely different deploy cadences (backend: continuous, on every push;
Android: manual, `make install` over USB, personal single-device app) and
that difference is worth keeping, not papering over.

## Decision

Merge both repos into one, `biwenger-client`, as `backend/` and
`android/` subdirectories:

- `biwenger-client-android`'s full commit history imported via `git
  subtree add --prefix=android` (not squashed) — preserved, not
  discarded.
- Root-level `docs/` holds what's genuinely stack-agnostic: `backlog/`
  (one file per feature, covering both sides of a slice from now on),
  `rat.md`, `biwenger-api-notes.md` (both stacks need the same Biwenger
  API knowledge), and `ways-of-working/` (git workflow, vertical
  slicing, backlog guide, concerns guide, test-refactoring-in-micro-steps
  — none of these were ever backend-specific in content, just
  backend-only in location).
- Each subdirectory keeps its own `AGENT.md`, `docs/adrs/`, and
  `docs/coding-conventions/` — checked before merging that there's no
  real overlap to consolidate (Android's 10 ADRs are about its
  event-driven MVI architecture; the backend's are about the Biwenger API
  and API Gateway keys — completely orthogonal decision streams).
- CI (`.github/workflows/backend-ci.yaml`) is path-filtered to
  `backend/**` so an Android-only commit doesn't trigger a spurious
  backend redeploy. No CI added for `android/` — it has none today and
  none is warranted for a personal, manually-installed app.
- `biwenger-client-android` archived on GitHub, read-only, README
  pointing here.

## Consequences

- One clone, one working tree, one backlog for a slice that spans both
  stacks — the `commit hash in the other repo` narration pattern goes
  away.
- Two toolchains (Node/npm, Kotlin/Gradle) coexist in one tree. Mechanical
  cost (two lockfiles, two sets of editor-open instructions), not a
  correctness risk — each subdirectory's tooling is self-contained and
  unaware of the other.
- Root `AGENT.md` now has to stay genuinely stack-agnostic, or content
  bleeds into the wrong subdirectory over time — worth a periodic check
  as both stacks evolve, not a one-time cost.
- The independent deploy cadence is preserved on purpose: a slice landing
  as one commit still deploys the backend half immediately and the
  Android half only at the next manual `make install`.
