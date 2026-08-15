See a player's market value history on the `PlayerDetailSheet` bottom
sheet (`SquadScreen.kt` — opened by tapping a player row on the squad
list) — a chart/list of past prices, scoped to the current season
including preseason (roughly July 1 onward; see
`docs/biwenger-api-notes.md` for the season-boundary rule and its
self-correcting date math). Backed by the `prices` field on the player
detail endpoint (`docs/biwenger-api-notes.md` — "Historical market
value"), which returns a trailing ~1 year of daily `[YYMMDD, price]`
entries, no auth required. The sheet already shows name/photo/current
price/points; this slice adds the price history alongside them — no new
screen or navigation route needed.

Shipped. Backend: new `GET /players/{playerId}/price-history` endpoint
(same native API Gateway key as `/squad`), returning
`{ seasonStart, prices }` — the full trailing window plus where the
current season starts within it, since the sheet ended up needing both a
"Last Year" and a "Current season" view (`src/price-history-view.js`).
The season-boundary rule is domain logic, not a Biwenger API quirk — it
lives in that file's comments/tests, not `docs/biwenger-api-notes.md`.

Android: `PlayerDetailSheet` renders a two-tab sparkline (gradient area +
line via Compose `Canvas`), price trend under the Price stat, and a
shimmering loading placeholder sized to match the loaded card exactly
(no layout jump). Fetching the tapped player's history needed two small
MVI framework extensions beyond the existing squad slice — event-
parameterized coeffects and dispatch-chaining a slow coeffect off a fast
state update — see `docs/adrs/ADR-009-event-parameterized-coeffects.md`
and `docs/adrs/ADR-010-dispatch-chaining-for-slow-coeffects.md` in
biwenger-client-android. Verified working on a physical device.
