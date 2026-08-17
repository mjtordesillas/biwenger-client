# ADR-006: Coeffects for Pure Event Handlers

**Status**: Accepted

---

## Context

Event handlers have the signature `(Event<T>, Coeffects) -> List<Effect>`
and are meant to be pure: given the same inputs, the same output.
`SquadViewModel.handleOnLoad` needs the fetched squad to decide what
effect to produce.

## Decision

External reads a handler needs are declared upfront as typed `Coeffect`
descriptors on the ViewModel (here, `FetchSquadCoeffect`). The framework
resolves them before invoking the handler and packages results into a
`Coeffects` map. The handler reads from `Coeffects` only — never touches
`Database` or a service directly.

```kotlin
private val squadCoeffect = FetchSquadCoeffect

fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
    listOf(UpdateState(path = "squad.players", value = coeffects.load(coeffect = squadCoeffect)))
```

## Consequences

- Positive: `handleOnLoad` is testable by constructing a `Coeffects` map
  directly — no service or database setup.
- Positive: `FetchSquadCoeffectHandler` is independently testable
  infrastructure with no ViewModel coupling.
- Negative: a new environmental read needs a new `Coeffect` subclass and
  handler registration — more setup than a direct call.
