Enrich "My Squad" player cards with facts that matter for
squad-management decisions, beyond name/position/price/points: how many
days left until the transfer lock lifts and the player can be listed on
the market, whether it's currently listed on the market, whether there's
a standing offer for it, and its fitness status (injured / in doubt for
the next match). All five are already available from data Biwenger's API
exposes today — see `docs/biwenger-api-notes.md` § "Squad player status
(owner lock, market listing, offers, fitness)" for exactly where each one
comes from. Requested together, shipping together, same precedent as
`view-current-market.md`'s slice 3 (expiry/seller/market value).

**Shipped** (2026-08-18): backend (`d977137`) adds `lockedUntil`,
`inMarket`, `hasOffer`, and `status` to `GET /squad` via
`squad-player-view.js`, cross-referencing `/market`'s sales/offers —
deployed and confirmed live. Android (`4b40e9a`) shows each as a badge
on the squad player card ("Sellable in 2 days", "Listed", "Offer
received", "Doubt"/"Injured"), rendering nothing when none apply.
Verified end-to-end on a real device against a real account/league —
see the commit messages and `docs/biwenger-api-notes.md` § "Squad
player status" for how each fact was found.
