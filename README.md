# Biwenger Client (Android)

Native Android client for [biwenger-client](https://github.com/mjtordesillas/biwenger-client)'s
backend. The backend is unusable from a plain browser URL once protected
by a native API key (see its `docs/adrs/002-native-api-gateway-key-for-squad-endpoint.md`) —
this app exists to be the caller that can actually hold and send that key.

## Tech Stack

Kotlin · Jetpack Compose · Material3 · Hilt · Retrofit

## Architecture

Ported from [interest-tracker-android](https://github.com/mjtordesillas/interest-tracker-android):
a custom event-driven architecture (MVI-like). UI dispatches events via a
central `Registry`; event handlers return effects; effect handlers execute
side effects. A path-based `Database` holds app state. See
[`AGENT.md`](AGENT.md) and [`docs/adrs/`](docs/adrs) for the full set of
decisions this was ported from.

## Setup

1. Create `local.properties` in the project root (already git-ignored).
2. Add your API key — the same value as biwenger-client's
   `BIWENGER_CLIENT_MOBILE_API_KEY`: `API_KEY=<value>`.

## Commands

| Command | Description |
|---------|--------------|
| `make build` | Build debug APK |
| `make clean` | Clean build outputs |
| `make test` | Run unit tests |
| `make test-connected` | Run instrumentation tests |
| `make install` | Build, test, and install on a connected device |
| `make reinstall` | Uninstall and reinstall on a connected device |
| `make launch` | Launch the app on a connected device |
