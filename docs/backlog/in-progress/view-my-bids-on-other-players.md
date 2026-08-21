View my own outgoing bids on other managers' players — the mirror image
of `view-offers-on-my-players` (that one is offers *received* on my
squad; this is offers *made* on someone else's). Card layout: owner of
the bid-on player top-left (header), when the bid expires top-right
(header) — same shape as `MarketListingHeader`/`PlayerOfferHeader`.
Content: the asking price grayed out, my offer amount underneath it, and
the difference between my offer and the catalogue market value (not the
asking price shown above it — this tells whether the bid is above/below
fair value, independent of what's being asked).

Verified against a real account (2026-08-22, placed a real bid to
check) — see `docs/biwenger-api-notes.md` § "Squad player status": a
`GET /market` `data.offers[]` entry has exactly one of `to`/`from`
populated, whichever side *isn't* the requester. My own outgoing bid has
`from.id` equal to my own user id and `to: null` (opposite of
`getOffersOnMyPlayers`' `to.id === userId` check). The bid's player had
a matching `data.sales[]` entry, so the owner/asking price for the
header and content can reuse the exact same sales join
`getCurrentMarket`/`getMyMarketListings` already do
(`sale.user?.name ?? null`, `sale.price`) — join `offer.requestedPlayers`
against `sales[]` by `player.id`, same shape as the offers-received join
already joins against the catalogue. Not yet confirmed: whether an
outgoing bid can target a player with no `sales[]` entry at all (an
unsolicited bid on an unlisted player) — if that turns out to happen,
owner/asking price would need a fallback (catalogue has no per-league
owner field to fall back to).

Lands as a fourth subtab under the Market section, alongside
`view-current-market`, `view-my-market-listings`, and
`view-offers-on-my-players` — same `MarketSubTab`/`MarketSubTabRow`
pattern, one more entry.
