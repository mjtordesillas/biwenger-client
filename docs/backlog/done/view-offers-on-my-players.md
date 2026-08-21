View incoming purchase offers on my own squad players — who's offering,
how much, and for which player. `enrich-squad-player-cards` already
surfaces a boolean "Offer received" badge per squad player from `GET
/market`'s `data.offers[]` (see `docs/biwenger-api-notes.md` § "Squad
player status" — `to.id`/`requestedPlayers` matching), but that's just a
flag; this is the actual detail behind it.

**Backend**: `getOffersOnMyPlayers()` (`biwenger-client.js`) filters
`data.offers[]` to `offer.to.id === userId`, flatMapping each offer's
`requestedPlayers` against the catalogue into `{offer, player}` pairs.
New `player-offer-view.js` (`toPlayerOfferView`) and
`player-offers-api-handler.js`, wired to `GET /market/offers`. Offers
turned out to carry `until` (expiry, unix seconds) after all — the
"worth re-checking" note above was about `from` (offering party),
which is still `null` in every sample seen; `until` was found while
re-checking a raw sample and is now surfaced too, undocumented before
this slice (see `docs/biwenger-api-notes.md` § "Squad player status").

**Android**: new `PlayerOffer` domain model (`price`/`amount`/`until`/
`bidder` — no separate asking-price split needed, unlike a listing,
since there's only one price here). `MarketSubTab` grew to its third
and final entry (Offers), `PlayerOfferRow` mirrors `MarketListingRow`'s
header/content/footer shape: header shows `From: <bidder>` (falling
back to `From: the Market` when `from` is null) on the left and
`Expires <relative time>` on the right, content shows the offer amount
against market value, footer shows market value + its increment. Sorted
soonest-to-expire first, same reasoning as `MarketListingOrder`. Tapping
a row opens the same shared player-detail sheet as the other two tabs.

Verified on a real device: the Offers tab shows real standing offers
with the correct amount/expiry/bidder label, and tap-through works.
