View the current transfer market.

Two increments, both shipped:

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

Verified end-to-end on a real device: the app shows real market listings
with the correct asking price (confirmed against a live API sample where
asking price and catalogue value differed).
