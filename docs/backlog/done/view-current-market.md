View the current transfer market.

Three increments, all shipped:

1. **Backend** — `GET /market` (`backend/src/market-api-handler.js`,
   `getCurrentMarket()` in `backend/src/biwenger-client.js`). Joins the
   league's current `sales` against the catalogue for name/position,
   overriding `price` with the listing's own asking price rather than
   the catalogue's live market value — the two diverge (see
   `docs/biwenger-api-notes.md` § "League transfer market"), and a
   market view is specifically about what a bid actually costs. Excludes
   the requester's own listings. Reuses `toPlayerView` unchanged.

2. **Android** — a `Market` tab (`android/app/.../features/market/`),
   reusing the `Player` model and `PlayerList`/`PlayerRow` composables —
   both promoted out of `features/squad` into shared locations
   (`domain/models/`, `ui/`) since this is the app's first second
   consumer of either. First app-wide navigation surface: this app never
   had more than one top-level screen before, so `core/navigation/`
   (`NavigationEffect`/`Navigator`/`NavigationProvider`) was ported in
   from `interest-tracker-android` rather than invented fresh, plus a
   two-item bottom nav bar. Slice 1 scope only — name, position, price;
   no expiry, seller, or balance/maxBid yet, deferred until real usage
   asks for them.

3. **Expiry, seller, and market value** — the three fields deferred from
   slice 2, done together on explicit request. Backend:
   `market-listing-view.js` (`toMarketListingView`) now shapes `{sale,
   player}` pairs into `price` (asking), `marketValue` (catalogue
   value, was silently overridden by slice 1's `price`), `until`
   (expiry, unix seconds), `seller` (name, or `null` for a free-agent
   listing) — `getCurrentMarket()` changed to return the pair rather
   than a merged/overridden object. Android: a feature-local
   `MarketListing` model (not `Player` — a listing has fields (asking
   price vs. market value, seller, expiry) a squad player doesn't, so it
   diverges rather than bolting nullables onto the shared model);
   `MarketListingRow` shows seller top-left, a relative expiry top-right
   ("in N hours" under 8h-away/today, "tomorrow", else "in N days" —
   `formatExpiry` in `MarketScreen.kt`, unit-tested for the day-boundary
   edge cases), and market value with its price-trend increment inline
   beneath the avatar/name/position row. `PlayerAvatar`/`PositionTag`
   (shared `ui/PlayerList.kt`) changed to take primitives instead of a
   `Player`, so `MarketListingRow` could reuse them without constructing
   a fake `Player`.

Verified end-to-end on a real device throughout: the app shows real
market listings with the correct asking price, and — for slice 3 — real
expiry/seller/market-value data alongside it.
