# Biwenger Client

A small, fast, ad-free alternative client for [Biwenger](https://biwenger.as.com).

Monorepo:

- [`backend/`](backend) — the unofficial Biwenger v2 API wrapper, exposed as a Lambda/API Gateway JSON API. Deploys continuously on push to `main`.
- [`android/`](android) — the native Android client that calls it. A personal app, installed manually via `make install` (USB/`adb`).

See [`AGENT.md`](AGENT.md) for shared ways of working, and each
subdirectory's own `AGENT.md` for stack-specific conventions.
[`docs/backlog/`](docs/backlog) tracks candidate features (organized by
state) — a slice frequently touches both subdirectories.

## Commands

Run from the root, namespaced `<stack>:<target>`:

| Command | Description |
|---|---|
| `make backend:test` | Run the backend test suite |
| `make backend:deploy` | Deploy the backend manually (normally CI does this on push) |
| `make android:build` | Build debug APK |
| `make android:clean` | Clean build outputs |
| `make android:test` | Run Android unit tests |
| `make android:test-connected` | Run Android instrumentation tests |
| `make android:install` | Build, test, and install on a connected device |
| `make android:uninstall` | Uninstall from a connected device |
| `make android:reinstall` | Uninstall and reinstall on a connected device |
| `make android:launch` | Launch the app on a connected device |

Each is a thin wrapper — `backend/package.json`'s npm scripts and
`android/Makefile`'s own targets are still there and still work directly
from within each subdirectory.
