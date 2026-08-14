# ADR-010: Dispatch-Chaining to Unblock Fast State Updates from Slow Coeffects

**Status**: Accepted

---

## Context

`ChannelRegistry` resolves every coeffect a handler declares *before*
invoking the handler (see ADR-006), and a single handler invocation
returns one `List<Effect>` applied together. `handlePlayerTapped`
originally declared `FetchPriceHistoryCoeffect` alongside its
`UpdateState(squad.selectedPlayerId, ...)` effect — so opening the sheet
(instant, from squad data already in memory) waited on the price history
network call (slow) before either effect was applied. The sheet felt
laggy to open for a reason that had nothing to do with opening it.

## Decision

Split the single event into two: `squad.player-tapped` sets
`selectedPlayerId` and `priceHistory = Loadable.Loading` with no
coeffects (fast, unblocked), then returns a `DispatchEvent` effect (see
`core/effects/DispatchEvent.kt`) for a new `squad.price-history-requested`
event. That second event carries the slow `FetchPriceHistoryCoeffect` and
updates `squad.priceHistory` on its own, once resolved.

`ChannelRegistry` processes events off one sequential channel, but
`Database.updateState` notifies subscribers synchronously as each effect
is applied — so the first event's `UpdateState`s reach the UI immediately
even though the channel loop won't pick up the dispatched second event
until the current handler returns.

```kotlin
fun handlePlayerTapped(event: Event<Int>): List<Effect> =
    listOf(
        UpdateState(path = "squad.selectedPlayerId", value = event.payload),
        UpdateState(path = "squad.priceHistory", value = Loadable.Loading),
        DispatchEvent(event = event(name = PRICE_HISTORY_REQUESTED_EVENT, payload = event.payload)),
    )

fun handlePriceHistoryRequested(event: Event<Int>, coeffects: Coeffects): List<Effect> =
    listOf(UpdateState(
        path = "squad.priceHistory",
        value = coeffects.load(coeffect = FetchPriceHistoryCoeffect(playerId = requireNotNull(event.payload)))
    ))
```

## Consequences

- Positive: the sheet opens instantly off data already in memory; only
  the price history section shows its own loading state, matching what's
  actually slow.
- Positive: both handlers stay pure/coeffect-driven per ADR-006 — no
  handler reaches for `Database`/services directly to "just fetch faster".
- Negative: one more named event and handler pair for what's
  conceptually a single user action, and the causal link between
  `player-tapped` and `price-history-requested` is implicit in the
  `DispatchEvent` payload rather than a single call stack.
