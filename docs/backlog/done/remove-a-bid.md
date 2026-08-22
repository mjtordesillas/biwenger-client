Remove one of my own outgoing bids, from the Bids tab
(`getMyBidsOnOtherPlayers`) — same style as `unlist-a-player`: a small
round button on the player card, tinted low-alpha background behind a
full-opacity glyph, same color schema. No confirmation popup — tapping
the button removes that bid directly. While the request is in flight,
the glyph swaps for a spinner on that same button (no double-submit).
On completion, the Bids list reloads so the removed bid disappears.
Applies to a standing bid on a free agent too.

Write endpoint RAT'd by capturing the real request live from Biwenger's
own web app via browser DevTools, verified 2026-08-22 against a real
outgoing bid (offer `4273101594`) — 204 No Content, bid gone from both a
follow-up `GET /market` and Biwenger's own UI. Confirmed the backlog's
open question: removing my own outgoing bid is `DELETE
/offers/{offerId}`, not the same `PUT {"status": ...}` shape
`reject-an-offer`/`accept-an-offer` use on the incoming side, even
though both read through the same `data.offers[]` array (an outgoing
bid is an `offers/{id}` entry, as guessed — just addressed differently).
See `docs/biwenger-api-notes.md` § "My outgoing bids — write (remove)".

**Backend**: `removeBidData`/`removeBid()` (`biwenger-client.js`), new
`remove-bid-api-handler.js`, wired to `DELETE
/market/my-bids/{offerId}` — a private write proxy, same
collapsed-upstream-error/no-credential-leakage shape as unlist/reject/
accept. `toPlayerBidView` (`player-bid-view.js`) gained `offerId` —
needed client-side to address the bid for removal, same as
`player-offer-view.js` already exposes for the incoming side.

**Android**: `PlayerBid` gained `offerId: Long`. `MarketService`/
`HttpMarketService` gained `removeBid(offerId)`. New `RemoveBidEffect`,
same shape as `UnlistPlayerEffect`'s. No confirmation dialog, unlike
reject/accept — tapping removes directly, so `market.removingBidIds` is
a *set* of in-flight offer ids, same reasoning as
`market.unlistingPlayerIds`. `PlayerBidRow` switched from a bare Column
to the same Box + overlay pattern `MarketListingRow` uses: a small round
button bottom-end, tinted low-alpha `TrendDown` background behind a
full-opacity `TrendDown` "x" glyph — identical schema to unlist's,
reusing `PlayerOfferActionButton` for the spinner-swap while in flight.

Verified live: the removal request/response shape above was captured
directly against a real bid via DevTools before writing any code.
