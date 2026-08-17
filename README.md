# Biwenger Client

A small, fast, ad-free alternative client for [Biwenger](https://biwenger.as.com).

Monorepo:

- [`backend/`](backend) — the unofficial Biwenger v2 API wrapper, exposed as a Lambda/API Gateway JSON API. Deploys continuously on push to `main`.
- [`android/`](android) — the native Android client that calls it. A personal app, installed manually via `make install` (USB/`adb`).

See [`AGENT.md`](AGENT.md) for shared ways of working, and each
subdirectory's own `AGENT.md` for stack-specific conventions.
[`docs/backlog/`](docs/backlog) tracks candidate features (organized by
state) — a slice frequently touches both subdirectories.
