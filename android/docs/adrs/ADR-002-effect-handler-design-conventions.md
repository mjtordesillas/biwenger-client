# ADR-002: Effect and Handler Design Conventions

**Status**: Accepted

---

## Context

Ported from interest-tracker-android's ADR-002. Applies once a feature
adds its own effects — squad currently produces none beyond the two core
effects (`DispatchEvent`, `UpdateState`), so this ADR is a convention
waiting for its first real use, not something exercised yet.

## Decisions

### 1. Collocate effect data class and handler in a single file
Place the effect data class and its handler in one file named after the
effect, inside the feature package that produces it. Effects with no
consumers outside their originating feature must not be moved to
`shared/`.

### 2. Pass the success event name as a `String` field on the effect
When a handler must notify the ViewModel on success, the effect carries
`successEvent: String`. The handler dispatches
`registry.dispatch(event(effect.successEvent))`. The ViewModel owns the
constant.

## Consequences

- Positive: effect and handler are always found together.
- Positive: effects are pure data classes — testable with structural
  equality; handlers have no ViewModel knowledge.
- Negative: a file containing two classes is slightly unconventional;
  acceptable because they are tightly coupled by design.
