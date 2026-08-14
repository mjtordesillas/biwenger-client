# ADR-003: Secret Injection via BuildConfig

**Status**: Accepted

---

## Context

The app needs the biwenger-client API key, which must not appear in
version-controlled source files.

## Decision

Store the secret in `local.properties` as `API_KEY=<value>`. Read it in
`app/build.gradle` via `buildConfigField` and expose it as a `BuildConfig`
constant. Consuming code (`AppModule.provideSquadService`) reads from
`BuildConfig.API_KEY` — never from `local.properties` directly. The build
fails at compile time if `API_KEY` is missing.

## Alternatives Considered

Same as interest-tracker-android's ADR-003: Android Keystore/
EncryptedSharedPreferences (rejected — doesn't solve build-time
injection), `System.getenv()` (rejected — Android processes don't inherit
shell env vars), bundled assets file (rejected — extractable from the
APK, no better than hardcoding).

## Consequences

- Positive: zero new dependencies; standard Android idiom.
- Negative: the secret is present in `BuildConfig.class` inside the APK in
  plaintext unless R8 minification is applied — not yet a concern at this
  app's stage.
