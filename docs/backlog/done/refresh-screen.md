Pull-to-refresh on every screen. Pulling down the list/grid reloads its
data from the server; while reloading, the screen goes blank and shows
the same loading spinner as the first-load state (not an in-place
spinner over stale content).

Backend-free — Android only, three screens: Squad (Players subtab),
Lineup, and Market (all four subtabs — Current Market, My Listings,
Offers, Bids).

The event framework already resolves coeffects (the actual network
fetch) *before* an event handler runs and its `UpdateState` effects
land — a single `ON_LOAD_EVENT` re-dispatch would swap stale content
directly for fresh content with no intermediate state, i.e. an in-place
swap, not the required blank-then-spinner. Each screen's ViewModel
gained a `REFRESH_REQUESTED_EVENT`/`handleRefreshRequested` that blanks
the relevant path(s) to `Loadable.Loading` first, then
`DispatchEvent`s `ON_LOAD_EVENT` to actually reload — the same two-step
Loading-then-DispatchEvent shape `handlePerformanceSeasonChanged`
(Squad/Market) and `handleSlotTapped` (Lineup) already used for a
different trigger. A `refresh()` dispatch method exposes it to the UI.

Market's `handleRefreshRequested` blanks all four subtab paths at once
and re-dispatches the same `ON_LOAD_EVENT` that already loads all four
together on first open — pulling on any one subtab refreshes the other
three underneath it too, same as opening the screen fresh does.

UI: each screen's content area is wrapped in Material3's
`PullToRefreshBox` (`androidx.compose.material3.pulltorefresh`, stable
since 1.3.0 — no dependency bump needed), `isRefreshing` bound directly
to that path's own `Loadable.Loading` check (the same check its
Loading/Failed/Success `when` already makes) rather than a separate
flag, so the built-in pull indicator and the blank-to-spinner content
swap stay in lockstep. Market wraps each of its four subtabs
independently inside the shared subtab `when`, each keyed to its own
Loadable, since only the currently-visible one should react to a pull
even though the refresh underneath reloads all four.
