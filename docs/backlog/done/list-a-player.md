List one of my squad players on the market, from the My Listings tab
(`view-my-market-listings`).

Like `unlist-a-player`, no reference-project hint could be trusted
(pablopb3/biwenger-api's `SendPlayersToMarket` used the wrong `type`
value and hardcoded a price it ignored its own parameter for) — RAT'd
by capturing the real request live from Biwenger's own web app via
browser DevTools, verified 2026-08-21 against a real listing (player
`15396`). See `docs/biwenger-api-notes.md` § "My market listings —
write (list)".

**Backend**: `listPlayer()` (`biwenger-client.js`), `POST .../market`
with `{"type":"sell","player":<id>,"price":<price>}`. New
`list-player-api-handler.js`, wired to `POST
/market/my-listings/{playerId}` — the fixed 35,000,000 asking price is
applied server-side, not client-supplied, matching "no price entry" in
the UI.

**Android**: `HttpClient` gained a no-body `post` (only `get`/`put`/
`delete` existed before); `MarketService` gained `listPlayer(playerId)`.
New `ListPlayerEffect`, same shape as `UnlistPlayerEffect`'s. A
right-aligned "List player" pill button (same tinted-background/
full-opacity-text purple as the confirmation dialogs' Cancel) sits above
the My Listings list, disabled once 5 players are already listed.
Tapping it opens a popup (`ListPlayerPopup`, ~90%/94% of the screen, on
the app's own background) showing the squad as cards
(`SquadListingCandidateCard`) sorted eligible-first. Eligibility is a
pure client-side check — `!inMarket && lockedUntil == null && count <
5` — deliberately *not* blocking on a pending offer, since Squad's own
"Listable ..." label only ever cares about the transfer lock and
listing works fine mid-offer in practice. An ineligible card is dimmed,
untappable, and shows why at the top ("Already listed", "Listable
{relative time}", or "Listing cap reached"). Tapping an eligible card
lists it directly (no confirmation dialog, same as unlist), shows a
spinner on that card while in flight, and the popup stays open —
`market.listingPlayerIds` is a set of in-flight ids, same reasoning as
`unlistingPlayerIds`. On completion, both the squad and my-listings data
reload so eligibility and the cap re-render correctly right away.

Verified on a real device: the button/popup/sizing/dimming/reason-text
all render correctly, and a real listing was created through the app
end to end.

Unblocks `cycle-player-listings` (to-do), now that both directions
(list/unlist) exist.
