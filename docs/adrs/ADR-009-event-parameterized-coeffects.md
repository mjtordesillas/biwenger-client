# ADR-009: Event-Parameterized Coeffects

**Status**: Accepted

---

## Context

ADR-006's `coeffects` list is fixed at handler-registration time (in the
ViewModel's `init`), which is enough when the read never varies — `squad.
on-load` always needs the same squad. `squad.player-tapped` breaks that
assumption: which player's price history to fetch depends on *which*
player was tapped, carried as the event's own payload. A statically
declared `FetchPriceHistoryCoeffect` has nowhere to get that id from.

## Decision

Added a second `registerEventHandler` overload where `coeffects` is a
function of the event (`(Event<T>) -> List<Coeffect<*>>`) instead of a
fixed list, resolved fresh on every dispatch. The original list-based
overload is untouched — handlers with no per-event parameter keep using
it (`squad.on-load` still does).

```kotlin
store.registerEventHandler(
    name = PLAYER_TAPPED_EVENT,
    coeffects = { event -> listOf(FetchPriceHistoryCoeffect(playerId = event.payload)) },
    handler = ::handlePlayerTapped
)
```

`FetchPriceHistoryCoeffect` is a `data class` (not an `object` like
`FetchSquadCoeffect`) so structural equality lets `coeffects.load(...)`
in the handler body look up the same resolved value by constructing an
equal instance — no identity/reference tricks needed.

## Consequences

- Positive: handlers stay pure and coeffect-resolution stays outside the
  handler body, matching ADR-006's intent, even when the read is
  parameterized by the event.
- Positive: purely additive — no existing call site changes.
- Negative: two ways to pass `coeffects` to `registerEventHandler` now
  exist; pick the list form unless the handler genuinely needs the
  event's payload to know what to fetch.
