# AGENT.md (android)

Stack-specific rules for `android/` — the native Android client (Kotlin,
Jetpack Compose). Process-level rules (git workflow, vertical slicing,
backlog, credentials, the manual-deploy-via-`make install` note) live in
the root `../AGENT.md` and apply here too. The backend this app calls is
`../backend`; API shape/quirks worth knowing on this side are in
`../docs/rat.md` and `../docs/biwenger-api-notes.md`.

## Conventions

### Always use the existing event-driven architecture
Never introduce a second architectural pattern alongside it. Do not
bypass `Registry` with direct coroutine launches. Every feature adds only:
effect data class(es), effect handler(s), any new service methods, and
ViewModel event handlers.

### Effect and handler design
Collocate the effect data class and its handler in one file named after
the effect, inside the feature package that produces it.

### Services
Every service must be a `@Singleton` provided via a `@Provides` function
in `AppModule`. The return type must be the service interface, not the
concrete implementation.

### Secrets
Store secrets in `local.properties` as `KEY_NAME=<value>`. Expose via
`buildConfigField` in `app/build.gradle`. Consuming code reads from
`BuildConfig` only — never from `local.properties` directly.

### Event naming
Events use dot-separated hierarchical names: `"squad.on-load"`.

### Named parameters
All function and constructor calls use named parameters, including
single-parameter calls and trailing lambdas. See
`docs/coding-conventions/named-parameters.md`.

@docs/coding-conventions/project-structure.md
@docs/coding-conventions/stateless-composables.md
@docs/coding-conventions/coeffects.md
@docs/coding-conventions/viewmodels.md

## Architecture

**Event → Effect → State.**

1. **Events** (`Event<T>`) — dispatched via `Registry.dispatch()`.
2. **Event Handlers** — registered on `Registry`, process events and
   return a list of `Effect`s.
3. **Effects** — side-effect descriptors handled by `EffectHandler`
   implementations.
4. **Database** — holds app state as a flat `Map<String, Any?>` keyed by
   dot-separated paths, `StateFlow`-backed.

### Key classes

- `Store` (`core/mvi/Store.kt`) — ViewModel-facing facade over `Registry`
  and `Database`.
- `Registry` / `ChannelRegistry` (`core/mvi/`) — central event dispatcher.
- `Database` (`core/state/Database.kt`) — state container with path-based
  subscribe/update.
- `StateInitializer` (`core/state/StateInitializer.kt`) — each feature
  declares its initial state slice; `AppModule` combines them.
- `EffectsHandlerRegistration` / `CoeffectsHandlerRegistration`
  (`shared/`) — startup wiring, one line per feature.
- `NavigationProvider` / `Navigator` / `NavigationEffect` / `Routes`
  (`core/navigation/`) — ported from interest-tracker-android once
  market became a second top-level screen. `NavigationProvider` holds
  the live `NavController` (set by `MainActivity`); dispatch
  `NavigationEffect.NavigateToRoute`/`PopBackStack` from an event
  handler like any other effect, or call `Navigator` directly from a
  Composable via straight `NavController.navigate()` for simple
  same-screen taps (see `MainActivity`'s bottom nav wiring).

### ViewModel pattern

Every screen has a `@HiltViewModel` that injects `Store`, subscribes to
paths, registers event handlers (removed in `onCleared`), and dispatches
an on-load event in `init`.

## Documentation

- `docs/adrs/` — Architecture Decision Records (Nygard format), ported
  from interest-tracker-android and adapted to this app.
- `docs/coding-conventions/` — naming, named parameters, project
  structure, stateless composables, coeffects, ViewModels.
- `docs/design-system/` — Nocturne, this app's design system.

Deliberately not ported yet: response caching (`ResponseCache`/
`CachingXxxService`) — no repeated-fetch cost has shown up yet. Add it
the slice that actually needs it, not before.

## Build & Test Commands

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
```
