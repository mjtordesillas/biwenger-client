# ADR-004: Services Are Singletons Provided via AppModule

**Status**: Accepted

---

## Context

`SquadService` is depended on by both `FetchSquadCoeffectHandler` and,
eventually, any other consumer. `AppModule` is the established place for
application-scoped bindings.

## Decision

`SquadService` is provided as a `@Singleton` via a `@Provides` function in
`AppModule`, typed as the interface (`SquadService`), not the concrete
`HttpSquadService`. `EffectsHandlerRegistration`/
`CoeffectsHandlerRegistration` receive it as a constructor parameter.

## Consequences

- Positive: exactly one instance at runtime; the handler depends on the
  port, not the adapter — swappable with a fake in tests.
- Negative: adding a second service means a constructor parameter on both
  `CoeffectsHandlerRegistration` and `AppModule` — mechanical, low-risk.
