See a player's per-gameweek points for the current and previous season
(no need for the full multi-year history) on the `PlayerDetailSheet`
bottom sheet (`SquadScreen.kt`), below the price-history chart added by
`add-price-history-trends`. Mockup provided (Nocturne design system
export, `Squad.dc.html`) shows a "Player performance" card:

- A season picker (dropdown) in the card header, independent of the
  price chart's own "Last Year" / "Current season" tabs above it. Mockup
  offers three seasons; this slice only needs current + previous.
- A horizontally scrollable bar chart, one bar per match day, labelled
  by match day number (1, 2, 3, ...) rather than a date.
- Bars diverge from a zero line — positive points grow upward, negative
  points (red cards, own goals, etc.) grow downward — with y-axis ticks
  at max/mid/min for the visible season.
- Bar color bands the score at a glance (mockup's demo thresholds: <2
  red, 2–6 yellow, 6–10 blue, 10+ green — illustrative, not necessarily
  the real Biwenger scoring bands).
- Tapping a bar toggles a tooltip: "Match day N: X pts".

Backing data confirmed available: `docs/biwenger-api-notes.md`
("Per-gameweek points via `reports`") — the player detail endpoint's
`reports` field, queried with a `season` id, returns one entry per round
with a points breakdown, no auth required. Need current + previous
season's `season` id per player, which comes from that same endpoint's
`seasons` field.

Progress: backend endpoint (`GET /players/{playerId}/performance-history`,
`?season=current|previous`, defaults to current) and Android's
"Player performance" card are both shipped, including the current/
previous toggle and magnitude-banded bar colors (<2 red, 2–5 yellow, 6–9
blue, 10+ green) — see biwenger-client commits `3dc4eda`/`3fb1ba9` and
biwenger-client-android commits `929e990`/`fd55546`/`c066494`. Not
deployed yet (`serverless deploy` not run), so it's not live for real
users.

Still open, deferred as thinnest-slice follow-ups:
- No y-axis tick labels or a visible zero-baseline line, though bars do
  grow up/down from zero.
- No tap-to-see-tooltip ("Match day N: X pts").
