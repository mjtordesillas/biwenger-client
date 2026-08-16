# Biwenger API Notes

Ongoing discoveries about Biwenger's undocumented `v2` API, made while
building specific features — as opposed to `docs/rat.md`, which is the
point-in-time record of the original Riskiest Assumption Test. Add to
this file as new endpoints/quirks turn up; it isn't testing an assumption,
it's just notes.

Explore these manually via the JetBrains HTTP Client:
[`requests/third-party/biwenger/biwenger-api.rest`](../requests/third-party/biwenger/biwenger-api.rest)
against the raw Biwenger API, or
[`requests/biwenger-client/squad.rest`](../requests/biwenger-client/squad.rest)
against our own deployed endpoint. Each directory has a committed
`http-client.env.json` (non-secret: base URLs) and a gitignored
`http-client.private.env.json` you fill in yourself (email/password, the
mobile API key) — never commit real values into the private file.

## Image CDN

Player photos and team crests aren't returned by any endpoint field —
built from path conventions, verified empirically (2026-08-14) by probing
candidate URLs against a real player/team id until one returned `200`:

- Player photo: `https://cdn.biwenger.com/i/p/{playerId}.png`
- Team crest: `https://cdn.biwenger.com/i/t/{teamId}.png`

Used in `src/player-view.js`.

## Position codes

`position` on a catalogue player is an integer: `1=GK 2=DF 3=MF 4=FW`.
`altPositions` is an array of the same codes for a secondary position —
usually absent, at most one entry seen so far (see `docs/rat.md` and
`src/player-view.js` for where `secondaryPosition` comes from).

## Historical market value

`GET https://biwenger.as.com/api/v2/players/la-liga/{playerId}?fields=id,name,prices`
No auth required. Verified empirically (2026-08-14) against
[`requests/third-party/biwenger/biwenger-api.rest`](../requests/third-party/biwenger/biwenger-api.rest).
Found by reading `players.go` in the
[pablopb3/biwenger-api](https://github.com/pablopb3/biwenger-api) reference
project (unofficial wrapper, not assumed correct — endpoint re-verified
against the real API before trusting it).

- The path segment documented elsewhere as a slug (e.g. `roger-brugue`)
  also accepts the plain numeric player id — no separate lookup needed.
- `data.prices` is `[[YYMMDD, price], ...]` — one entry per day, price in
  the same units as the catalogue's `price` field (whole units, no
  decimals).
- The array is a **trailing window, not full history**: player 15396
  returned exactly 366 entries spanning 2025-08-14 → 2026-08-14 (today).
  Older data isn't available through this endpoint — anything beyond ~1
  year back would need to be captured/stored ourselves going forward.

## Points breakdown on the catalogue endpoint

`GET https://biwenger.as.com/api/v2/competitions/la-liga/data?lang=es&score=5`
(the same catalogue endpoint `getCatalogue()` already calls in
`src/biwenger-client.js`) returns, per player, more than the `points`
total `player-view.js` currently keeps: `pointsLastSeason` (previous
season's total), plus this-season `playedHome`/`playedAway` (games played)
and `pointsHome`/`pointsAway`. No per-gameweek breakdown is exposed here —
verified empirically (2026-08-15) via a direct, unauthenticated curl.

Sampled during preseason (2026/2027 season not yet started — all rounds
`status: "pending"`), so this-season fields were mostly `0` for the
players checked; `pointsLastSeason` was already populated (e.g. `162`)
since the prior season is complete. Worth re-checking once the season is
underway before relying on the this-season fields.

## Per-gameweek points via `reports`

`GET https://biwenger.as.com/api/v2/players/la-liga/{playerId}?fields=id,name,reports&season={seasonId}`
No auth required. Verified empirically (2026-08-15).

- `data.reports` is an array, one entry per round the player's team
  played, each with `match.round.name`/`.short` (e.g. `"Round 1"`/`"R1"`),
  `match.date`, and `points` — a dict keyed by scoring-format id (`"5"`
  is the id the catalogue endpoint already uses via `score=5`). A report
  can be `null` (e.g. the player didn't feature that round — see `R38`
  for player 15396) or have `points[format]: null` even when the report
  itself exists.
- Summing `points["5"]` across a season's reports reproduces the
  catalogue's `pointsLastSeason` exactly (checked against player 15396,
  Brugué: 38 reports → sum `44`, catalogue `pointsLastSeason: 44`).
- `season` takes a **season id**, not a slug or year range — the id is
  the season's end year as a string (`"2026"` for 2025/2026, `"2027"`
  for 2026/2027, etc.). Get the id for a given player from
  `fields=seasons`, which also lists point totals per format for every
  season the player has data for (including past competitions if
  they've changed leagues, each flagged with its own `competition` and
  `player.id`/`player.slug` — that cross-competition case doesn't apply
  to two players staying in la-liga across seasons).
- Omitting `season` defaults to the current season if it has started, or
  falls back to the last completed one otherwise (during today's
  preseason, no `season` param and `season=2026` returned identical
  results for a 2025/26 la-liga player).
- No pagination knob found (`reports(100)`, `reports(50,0)` etc. all
  no-ops) — a season's reports just come back as one array, which is
  fine since a la-liga season is capped at 38 rounds.
