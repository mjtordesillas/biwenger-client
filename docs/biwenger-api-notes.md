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

`https://cdn.biwenger.com/i/p/0.png` — player id `0` specifically — is
Biwenger's own generic placeholder (a plain gray head-and-shoulders
silhouette, 160x160), not a 404. Verified empirically (2026-08-18) by
probing candidate paths after `view-my-lineup` needed one for a vacant
lineup slot (Biwenger's own lineup data can be short of a formation's
full count — see "Starting lineup" below). Confirmed distinct from a
real player photo by content (grayscale silhouette vs. a real color
headshot) and by size (~2KB vs. tens of KB).

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

## League transfer market

`GET https://biwenger.as.com/api/v2/market`
Requires `Authorization: Bearer <token>` **and** `X-League`/`X-User` headers
(same shape as `getSquadPlayerIds`) — 400 `"X-League and X-User headers
required"` without them. Verified empirically (2026-08-17) against a real
league via `requests/third-party/biwenger/biwenger-api.rest`. Found via the
`market.go` handler in the
[pablopb3/biwenger-api](https://github.com/pablopb3/biwenger-api) reference
project (unofficial wrapper, not assumed correct — re-verified against the
real API before trusting it).

- `data.sales[]` is the actual market listing — one entry per player
  currently up for sale, each `{date, until, price, player: {id}, user}`.
  `date` is when it was listed, `until` is expiry, both unix seconds.
  No name/position/etc — join against the catalogue (`getCatalogue()`),
  same pattern as `getMySquad`.
- Two kinds of entries, told apart by `user`:
  - **Free-agent listings**: `user: null`, `player` has only `id`.
  - **Manager clause-buys**: `user: {id, name, icon}` (the seller),
    `player.owner.clause` present and equal to `price`. This includes the
    authenticated user's own listings (`user.id` == the requester's own
    account id) — filter those out for a "players I can bid on" view, same
    as `market.go`'s `IsMyPlayer` check.
- `data.status` is `{balance, maximumBid}` for the authenticated user —
  rides along on the same call, not part of the listings themselves.
- `data.offers[]` is a separate concern: incoming purchase offers on
  specific owned players (`requestedPlayers`, `amount`, `status`, `to`) —
  not part of "view the market", closer to a negotiation/recommendations
  feature.

## Squad player status (owner lock, market listing, offers, fitness)

Verified empirically (2026-08-18) against a real account/league, for
`enrich-squad-player-cards`.

- **Transfer lock** — `GET /user?fields=players(id,owner)` (the same call
  `getSquadPlayerIds` already makes) returns more on `owner` than `date`:
  for a player bought from the market (not owned since the league draft),
  `owner.lockedUntil` is a unix-seconds timestamp for when the player
  becomes sellable/listable again (observed 1-2 days out from the
  purchase). Absent entirely for players already past their lock (or
  never locked, e.g. draft-owned) — treat missing as "sellable now", not
  as `0`/`null` meaning "locked forever". `owner.price` (what was paid)
  and `owner.clause` (buyout clause, when the club/league sets one) are
  also present but unrelated to the lock.
- **Currently listed on the market** — same shape `getCurrentMarket`
  already fetches from `GET /market`'s `data.sales[]`, just not filtered
  out: a sale entry with `sale.user.id` equal to the requester's own user
  id (excluded by `getCurrentMarket`, since that endpoint is "what can I
  bid on") means *I've* listed that player. Every clause-buy sale
  observed had `sale.price` equal to `sale.player.owner.clause` — makes
  sense, a clause-buy's asking price is always the clause value.
- **Standing offer on an owned player** — same `GET /market` response,
  `data.offers[]` (noted as out-of-scope for `view-current-market`,
  relevant here): each entry has `requestedPlayers: [playerId, ...]`,
  `to: {id, name, icon}` (whoever's receiving the offer), `from` (`null`
  in every sample seen — the offering party wasn't identifiable from
  this field), `amount`, `status` (`"waiting"` in every sample), `type:
  "purchase"`. An offer on one of my players has `to.id` equal to my own
  user id and my player's id in `requestedPlayers`.
- **Fitness status** — the catalogue endpoint's per-player `status` field
  (already fetched by `getCatalogue()`, never surfaced) is one of `"ok"`
  (511/566 players in the sample), `"injured"` (33), `"doubt"` (13),
  `"sanctioned"` (4), `"unknown"` (4), `"discarded"` (1 — presumably
  retired/left the league, not seen elsewhere). There's also a `fitness`
  array (recent-match fitness/points history, mixes numbers and status
  strings) that wasn't needed for this feature and isn't otherwise
  understood yet.

## Starting lineup

`GET https://biwenger.as.com/api/v2/user?fields=lineup(type,playersID)`
Headers: `Authorization`, `X-League`, `X-User` (same shape as
`getSquadEntries`). Verified empirically (2026-08-18) against a real
account, for `view-my-lineup`. Found via `lineup.go` in the
[pablopb3/biwenger-api](https://github.com/pablopb3/biwenger-api)
reference project (unofficial wrapper, not assumed correct — the field
shape re-verified against the real API before trusting it; that
project's own fetch also chains in `players`/`market`/`offers` on the
same call, not needed here since `getMySquad`/`getCurrentMarket` cover
those separately).

- `data.lineup.type` is the formation as a hyphen-separated string of
  outfield player counts, e.g. `"3-5-2"` (3 defenders, 5 midfielders, 2
  forwards — goalkeeper is implicit, always exactly 1, not counted in
  the string).
- `data.lineup.playersID` is an **ordered** array of 11 ids — not
  arbitrary order. Cross-checked against the real account's formation
  (`"3-5-2"`) and each id's catalogue `position`: the array is exactly
  `[goalkeeper, defender × 3, midfielder × 5, forward × 2]` — i.e.
  grouped back-to-front by position, counts matching the formation
  string (plus the always-1 goalkeeper first). Parsing the formation
  string's three numbers plus the fixed goalkeeper slot is enough to
  slice `playersID` into its position groups without any extra field.
- `data.lineup.date` is a unix-seconds timestamp, presumably the
  fixture/matchday this lineup applies to — not investigated further,
  not needed for `view-my-lineup`.
- No `fields=players` join needed for name/photo/team — the ids in
  `playersID` are catalogue ids, joinable via `getCatalogue()` exactly
  like `getSquadEntries`'s ids.
- A vacant slot (formation has a spot the manager hasn't filled) is
  presumed to simply shorten `playersID` below 11, rather than a null/
  placeholder id — **not independently verified** (would require
  leaving a real slot vacant on a live account to observe), going on
  the user's own report of Biwenger's behavior. `view-my-lineup`'s
  Android side treats any shortfall against the formation's expected
  per-band count as vacant slots, rendered with the id-`0` default
  photo (see "Image CDN" above) rather than left undrawn.

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

## Score format ids, and no per-event points breakdown

`GET https://biwenger.as.com/api/v2/competitions/la-liga/data?lang=es&score={id}`
(`fields=` is accepted syntactically but ignored — always returns the
full shape). Verified empirically (2026-08-16).

- `data.scores` lists every `score=` id with its label: `1`="Diario AS",
  `7`="Feeberse Score", `8`="Media AS y Feeberse", `2`="SofaScore",
  `5`="Media AS y SofaScore" (an average, not AS alone), `3`="Estadísticas",
  `6`="Biwenger Social". The already-shipped performance-history chart
  reads `points["5"]` (inherited from `getCatalogue()`'s `score=5`,
  itself inherited from a reference project during the RAT, never
  verified against this label list) — that's "Media AS y SofaScore", not
  Diario AS. Not corrected without a concrete need (the chart's UI never
  claims to be showing "Diario AS"), but `points["1"]` is the key for any
  feature that specifically wants the Diario AS number.
- `reports[].points` and `reports[].rawStats` only ever carry each
  report's **total** per score format (e.g. `points: {"1": 6, "5": 5,
  ...}`) — never a breakdown by event/category, even for reports
  containing goals/assists/cards (checked player 15396/season 2026,
  rounds R7/R14/R36/R37 — assist, yellow card, goal, red card
  respectively). No `fields=` combination on this or the reports
  endpoint surfaces per-event point values; they don't appear to be
  exposed by this API at all. The per-event breakdown below was instead
  reverse-engineered by comparing reports with/without each event type
  against `rawStats.picas`/`rawStats.sofascore` (see next section).

## Diario AS / SofaScore scoring formula (reverse-engineered)

Not exposed by any endpoint field — reconstructed by diffing
`reports[].points`/`rawStats` across ~800 reports (season 2026, ~115
players spanning all four positions, plus a 49-goalkeeper-only pass) for
reports with/without each event type, then cross-checked against the
real Biwenger app. Verified empirically (2026-08-16).

**Diario AS (`points["1"]`)** = `picas_base(rawStats.picas)` + `goal_bonus[position]` (once per goal) + `3` per penalty goal (flat, any position) − `6` per `rawStats.redCard` − `3` per `rawStats.secondYellowCard` + `0` per assist + `0` per yellow card:

`redCard` and `secondYellowCard` are separate `rawStats` fields, not
reliably distinguishable from `events` alone (a report with `events`
types `6` then `7` close together — textbook second-yellow shape — had
`redCard: 1, secondYellowCard: null` in one sample; the field itself is
the source of truth, not the event sequence). Only one
`secondYellowCard` sample found (`-3` delta) against three `redCard`
samples (`-6` delta each) — thin on the second-yellow side, but a real,
reproducible field-level distinction, not noise.

| `picas` | base pts |
|---|---|
| 0 | -2 |
| 1 | 2 |
| 2 | 6 |
| 3 | 10 *(extrapolated from the +4/pica pattern, not directly sampled)* |
| 4 | 14 *(extrapolated, not directly sampled)* |
| `"SC"` (string — chronicler didn't rate the player; always paired with `minutesPlayed` in the low single digits) | 0 |

**SofaScore (`points["2"]`)** = `sofascore_base(rawStats.sofascore)` + `goal_bonus[position]` + `1` per assist + `0` per red/yellow card (a red card's effect is already priced into a lower `sofascore` rating, no separate penalty on top):

| `sofascore` rating | base pts | | rating | base pts |
|---|---|---|---|---|
| 9.5–10.0 | 14 | | 6.8–6.9 | 4 |
| 9.0–9.4 | 13 | | 6.6–6.7 | 3 |
| 8.6–8.9 | 12 | | 6.4–6.5 | 2 (standard baseline) |
| 8.2–8.5 | 11 | | 6.2–6.3 | 1 |
| 8.0–8.1 | 10 | | 6.0–6.1 | 0 |
| 7.8–7.9 | 9 | | 5.8–5.9 | -1 |
| 7.6–7.7 | 8 | | 5.6–5.7 | -2 |
| 7.4–7.5 | 7 | | 5.4–5.5 | -3 |
| 7.2–7.3 | 6 | | 5.2–5.3 | -4 |
| 7.0–7.1 | 5 | | 5.0–5.1 | -5 |
| | | | 0.0–4.9 | -6 |

Verified exact (319/319 clean reports with no goal/assist/card that
match day) — every `sofascore`→`points["2"]` pair in the sample landed
on the table above with zero mismatches.

**`goal_bonus[position]`** (same table for both formats): GK `+6`, DF
`+5`, MF `+4`, FW `+3`. GK is a promise from a domain expert rather than
a sampled report (no goalkeeper scored in the ~800-report sample), but
follows the same +1-per-tier pattern the other three positions show
exactly, so treated as confirmed, not estimated.

**`points["5"]`** ("Media AS y SofaScore", what the already-shipped
performance-history chart reads) = `round_half_away_from_zero((points["1"] + points["2"]) / 2)`
— i.e. `.5` rounds toward the larger magnitude, not always toward
`+Infinity` (e.g. average `-2.5` → `-3`, not `-2`). Verified exact
(468/468 reports with all three fields present).

Cross-checked against 5 screenshots of the real Biwenger app's own player
detail sheet (2026-08-16) — every picas/sofascore/goal/penalty/assist
figure above matched exactly (e.g. Guridi: 2 picas → `+6`, 1 penalty goal
→ `+3` both formats, 6.7 sofascore → `+3`; Tenaglia: 3 picas → `+10`, 1
goal as DF → `+5` both formats, 8.6 sofascore → `+12`). Also settled from
those screenshots:

- A bonus event that contributes `0` to a given format gets **no row at
  all** in that format's breakdown (not a row showing `+0`) — e.g. an
  assist only ever appears under the SofaScore block, never under AS.
- `rawStats.redCard` and `rawStats.secondYellowCard` are **separate
  fields** (missed on the first pass, which only used `events` types `6`
  and `7` together to infer a second-yellow red) — no need to infer a
  double-yellow from combined event types, `rawStats` says so directly.
  Still only the same 1-2 samples confirming both score `-6` in AS; not
  re-verified after finding the dedicated field (hit a 429 rate limit
  mid-check).
- `rawStats` also has `penaltyMissed` and `ownGoals` fields, unsampled
  (no occurrences seen) — presumably `0`/negative bonuses respectively,
  not derived yet.
- **"Súper Pica"** (Guridi screenshot: a `+3` line with a spade icon,
  read as AS-only given `config.superPicaScores: [1,5,8]` from the
  catalogue endpoint listing exactly the AS-involving formats) and an
  **"MVP" star badge** (Tenaglia screenshot, no points tied to it that
  aren't already accounted for by his goal bonus) are both real but
  **not implemented** — neither corresponds to any `rawStats` field
  found across ~800 reports scanned, so there's no known way to derive
  either from this API yet. Deliberately out of scope for
  `view-match-day-details`, not something the feature quietly gets
  wrong.
