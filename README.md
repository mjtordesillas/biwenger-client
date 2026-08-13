# Biwenger Client

A small, fast, ad-free alternative client for [Biwenger](https://biwenger.as.com).

Not a rebuild of Biwenger. A thin client that exposes only the parts of our
league we actually use, built as a sequence of tiny, independently
deployable vertical slices (Lean/Agile, Trunk-Based Development, Elephant
Carpaccio — see `docs/`).

## Status

**Riskiest Assumption Test (RAT): PASSED.**

We confirmed we can authenticate against the current Biwenger v2 HTTP API
with email/password and retrieve the authenticated user's squad (player
name, position, market value) using 4 reproducible HTTP calls. Details and
the experiment script are in [`docs/rat.md`](docs/rat.md).

Currently building **Slice 1**: open a URL, see your current squad, no ads.

## Principles

- Every commit to `main` should be deployable.
- Every slice is a complete, tiny piece of user value — not a layer of
  architecture.
- No abstractions before the second use case demands them.
- Read-only first. No automated bidding. No credentials in source control.

## Repo layout (evolves as slices are added)

```
docs/     RAT notes, decisions
scripts/  small experiment/ops scripts (e.g. the RAT script)
```
