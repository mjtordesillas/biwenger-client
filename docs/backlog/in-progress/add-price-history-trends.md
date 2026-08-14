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
