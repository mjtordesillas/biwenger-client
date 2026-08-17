# Biwenger Client (Android)

Native Android client for [`../backend`](../backend). The backend is
unusable from a plain browser URL once protected by a native API key (see
its `docs/adrs/002-native-api-gateway-key-for-squad-endpoint.md`) — this
app exists to be the caller that can actually hold and send that key.

Formerly its own repo (`biwenger-client-android`), merged into this
monorepo — see `../docs/adrs/` for why.

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
3. In Android Studio, set **Gradle JDK** (Settings → Build, Execution,
   Deployment → Build Tools → Gradle) to **"Use Gradle from 'JAVA_HOME'
   environment variable"** — matching interest-tracker-android's
   `#GRADLE_LOCAL_JAVA_HOME` setup — rather than Android Studio's bundled
   JDK. The bundled JDK has been ahead of what the Gradle wrapper version
   supports (symptom: *"The project's Gradle version ... is incompatible
   with the Gradle JVM version ..."* on sync); `JAVA_HOME` here resolves
   (via SDKMAN) to a compatible JDK 17.

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
