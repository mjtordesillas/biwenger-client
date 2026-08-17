# ADR-008: Coeffects Deliver `Loadable<T>`

**Status**: Accepted

---

## Context

A failed squad fetch must be distinguishable from a genuinely empty
squad — silently showing "no players" on a network error would be wrong.

## Decision

`FetchSquadCoeffectHandler.extract` throws (`SquadFetchException`) on
failure rather than returning a default. The framework catches it, calls
`onFailure` (default: wraps in `Loadable.Failed`), and delivers
`Loadable<List<Player>>` to `handleOnLoad`. `SquadScreen` pattern-matches
`Loadable.Loading` / `Loadable.Success` / `Loadable.Failed` to render a
spinner, the list, or an error message respectively.

## Consequences

- Positive: one pattern for the async load; loading/failure are
  first-class state, not an ad hoc boolean flag.
- Negative: every async-loaded UI path requires a three-way `when` — one
  extra branch versus assuming success.
