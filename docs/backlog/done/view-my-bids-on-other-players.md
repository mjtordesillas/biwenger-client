View my own outgoing bids on other managers' players — the mirror image
of `view-offers-on-my-players` (that one is offers *received* on my
squad; this is offers *made* on someone else's).

Verified against a real account (2026-08-22, placed a real bid to
check) — see `docs/biwenger-api-notes.md` § "Squad player status": a
`GET /market` `data.offers[]` entry has exactly one of `to`/`from`
populated, whichever side *isn't* the requester. My own outgoing bid
had `from.id` equal to my own user id and `to: null` (opposite of
`getOffersOnMyPlayers`' `to.id === userId` check), and a matching
`data.sales[]` entry — so the owner/asking price for the card can reuse
the exact same sales join `getCurrentMarket`/`getMyMarketListings`
already do, rather than the catalogue-only join the offers-received
side uses.

**Backend**: `getMyBidsOnOtherPlayers()` (`biwenger-client.js`), new
`player-bid-view.js` (`toPlayerBidView`) and `player-bids-api-handler.js`,
wired to `GET /market/my-bids`.

**Android**: new `PlayerBid` domain model (`MarketListing`'s shape —
asking price, market value, seller, expiry — plus `amount`, my own
bid). `MarketSubTab` grew to its fourth and final entry (Bids);
`PlayerBidRow` reuses `MarketListingRow`'s header/footer shape (owner
top-left, expiry top-right; market value + increment footer) with a
three-line content column: asking price grayed out, my offer
underneath it, and the offer-vs-market-value delta below that (diffed
against market value, not the asking price shown above it — confirmed
explicitly, tells whether the bid is above/below fair value independent
of what's being asked).

**Follow-up polish**, done in the same slice once all four subtabs
existed side by side: `MarketSubTabButton`/`SquadSubTabButton` changed
from icon-and-label-in-a-row to stacked (icon above label) — four tabs
crowded "Current Market"/"My Listings" at equal width. "Current Market"
shortened to "Market" (redundant next to its own siblings). My
Listings/Offers icons settled on `LocalOffer` (a listing is an offer to
sell) and outlined `Payments` (an incoming purchase offer is money
coming in) respectively, after a swap from the initial pairing.

Verified on a real device: the Bids tab shows the real outgoing bid
placed to explore the API, with correct owner/expiry/asking
price/offer/delta, and tap-through works.
