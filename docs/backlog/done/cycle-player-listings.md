Cycle my market listings in one tap — unlist everything currently on
the market and list up to 5 new candidates in their place. A "Cycle
listings" button top-left on the My Listings tab, mirroring
`list-a-player`'s "List player" button top-right on the same tab.

No confirmation popup — direct action, spinner-on-button feedback, same
as `list-a-player`/`unlist-a-player`.

No real listing-history API exists (checked, same RAT approach as
list/unlist — no reference project has it either), so selection
degrades to the agreed fallback: no-standing-offer candidates first,
falling back to with-offer ones only to fill remaining slots. Excluding
the outgoing listings from the new selection is free rather than
special-cased — the squad/market snapshot is taken *before* any unlist
write runs, so the outgoing 5 still show `inMarket: true` at selection
time, and the ordinary eligibility check already excludes them.

**Backend, not Android** (a mid-course correction from a first
Android-only version — see git history): `cycleListings()`
(`biwenger-client.js`), `POST /market/cycle-listings` — one login, one
account lookup, one squad+market fetch, then every unlist/list write
for the batch fires in parallel reusing that session
(`Promise.allSettled`, so one failing write doesn't abort the rest).
Moving this server-side beats firing up to 10 independently-
authenticated calls from the client, and is what makes a future
scheduled/automated trigger possible without the app — not built yet,
this slice is just the endpoint. `selectPlayersToList` is exported for
narrow unit testing; `getMySquad`'s squad/catalogue/market join logic
was extracted into a shared `buildSquadTuples` helper, reused by both.
`DEFAULT_LISTING_PRICE` moved from `list-player-api-handler.js` into
`biwenger-client.js`, now shared with `cycleListings`.

**Android**: `MarketService` gained `cycleListings()` — one HTTP call,
via the same no-body `post()` `HttpClient` gained for `listPlayer`. New
`CycleListingsEffect` (no payload — the selection is opaque to the
client now). `market.cyclingListings` is a single boolean (not a set of
ids, since it's one HTTP call, not up to 10) driving the Cycle button's
own spinner; "List player" is disabled while it's true, alongside its
existing 5-listing cap check. Per-row spinners on individual My
Listings cards during a cycle no longer apply — the client doesn't know
which ids the backend picked until the reload after it finishes, so the
whole list just refreshes to the new 5 once the call returns.

Verified on a real device: tapping Cycle shows the button's own
spinner until the backend call returns, List player stays disabled
meanwhile, and the list settles to the new 5 once it finishes.
