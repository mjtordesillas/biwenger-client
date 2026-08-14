# ADR-005: Split Screen Composables into Stateful and Stateless Layers

**Status**: Accepted

---

## Context

`hiltViewModel()` requires the Hilt component hierarchy, which is absent
during Compose preview. A composable that injects it directly can't be
annotated `@Preview`.

## Decision

`SquadScreen` (and every future screen composable) is split into a public
stateful overload (`hiltViewModel()` + state observation, delegates
rendering) and a private stateless overload (layout only, explicit props,
no ViewModel).

## Consequences

- Positive: the stateless overload can be `@Preview`-annotated and
  independently tested with Compose testing tools.
- Negative: two overloads instead of one — small boilerplate increase.
