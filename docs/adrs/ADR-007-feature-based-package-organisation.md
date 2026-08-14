# ADR-007: Feature-Based Package Organisation

**Status**: Accepted

---

## Context

With only one feature (`squad`), a package structure decision made now
determines how cheaply a second feature can be added later without
coupling to the first.

## Decision

```
com.biwenger_client/
├── core/           # MVI framework only — events, effects, coeffects, state, registry
├── features/       # One subpackage per feature: domain/ + infrastructure/ + ui/
├── infrastructure/ # Cross-feature technical concerns (HTTP client)
├── shared/         # AppModule, EffectsHandlerRegistration, CoeffectsHandlerRegistration
└── ui/             # Global Compose theme
```

`core/` contains only feature-agnostic architecture code. `shared/`
contains only startup wiring, no feature logic. Feature-specific models,
effects, and coeffects live in `features/squad/domain/`.

## Consequences

- Positive: adding a second feature means adding a new package under
  `features/` without modifying `squad/`'s package.
- Negative: with one feature, "cross-feature shared UI" has no obvious
  home yet — deferred until a second feature actually needs something
  shared, per interest-tracker-android's ADR-007's same trade-off.
