Unlist one of my own market listings, from the My Listings tab
(`view-my-market-listings`).

No reference-project hint existed for the write endpoint this time
(unlike `reject-an-offer`/`accept-an-offer`) — RAT'd by capturing the
real request live from Biwenger's own web app via browser DevTools,
verified 2026-08-21 against a real listing (player `37817`). See
`docs/biwenger-api-notes.md` § "My market listings — write (unlist)".

**Backend**: `unlistPlayer()` (`biwenger-client.js`), `DELETE
.../market?player={playerId}` — no request body, keyed on the player id
via a query param (a user can only have one active listing per player;
the raw `sale` shape has no `id` of its own). New
`unlist-player-api-handler.js`, wired to `DELETE
/market/my-listings/{playerId}` — a private write proxy, same
collapsed-upstream-error/no-credential-leakage shape as reject/accept.

**Android**: `HttpClient` gained a `delete` method (only `get`/`put`
existed before); `MarketService`/`HttpMarketService` gained
`unlistPlayer(playerId)`. New `UnlistPlayerEffect`, same shape as
`RejectOfferEffect`'s. No confirmation dialog, unlike reject/accept —
tapping the button unlists directly, so `market.unlistingPlayerIds` is
a *set* of in-flight ids rather than a single dialog-gated id/boolean
(multiple rows can plausibly be tapped in quick succession). On each of
my listing cards, a small round button — tinted low-alpha `TrendDown`
background behind a full-opacity `TrendDown` "x" glyph, same schema as
reject's — overlaid bottom-right; while in flight, the glyph swaps for
a spinner on that same button. `MarketListingRow`/`MarketListingList`
(shared with the Current Market tab, which can't unlist anything) gained
an optional `onUnlistTapped`/`unlistingPlayerIds`, defaulting to
off/empty so Current Market is unaffected. `PlayerOfferActionButton`
(from `accept-an-offer`) gained a `loading` parameter reused here for
the spinner-swap, rather than a new button component.

Verified on a real device: the unlist button appears only on My
Listings, tapping it shows a spinner on that row, and the listing
disappears from the list on success once the row's write finishes.

Unblocks `cycle-player-listings` (to-do), which needs this exact
"unlist my own listing" action to clear the 5 current listings before
relisting new ones.
