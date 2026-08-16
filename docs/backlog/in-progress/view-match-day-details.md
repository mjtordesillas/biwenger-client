Tapping a bar on the "Player performance" chart (from
`view-player-performance-history`) opens a detail view for that match
day. That feature shipped without a tap interaction at all (its
mockup's tap-to-tooltip was dropped, not deferred), so this is the tap
handler's first implementation, not a replacement for one.

Modeled directly on Biwenger's own player detail sheet (screenshotted
from the production app, 2026-08-16), not the original Nocturne mockup —
the real app doesn't show a match day/kickoff date in this sheet at all
(it lives on the parent match screen instead), but we want it here
anyway since this view isn't reachable from that parent screen.

Header:
- Player photo, team crest, position badge — already available from
  existing squad data, not new.
- Home crest — score — away crest (`match.home`/`match.away`, each with
  `id`/`name`/`score`; crest built from the team id per the Image CDN
  convention already used for players).
- The match day and kickoff, e.g. "Match day 8 | Mar 10 (Tue) - 13:00"
  (`match.round` for the match day number, `match.date` — a unix
  timestamp — for the formatted date/time). Not in the real app's own
  sheet (confirmed via screenshot) — an intentional addition since nothing
  else in this flow shows it.
- The total points for whichever score format the league actually uses
  (same one the existing performance-history chart's bars already show).

Body — two parallel breakdown blocks, one per score format, each
structured base-rating-row + one row per bonus that actually contributed
(a bonus contributing `0` gets no row at all — confirmed from
screenshots), then a combined "Media: (AS + SofaScore) / 2" row:

- **Diario AS** (`points["1"]`): `picas` row (e.g. "2 Picas ♣♣ · +6"),
  then a row per goal ("1 Gol · +3", position-scaled), per penalty goal
  ("1 Gol de penalti · +3 flat"), per red card ("+-6", direct or
  second-yellow, same value). No row for assists or yellow cards — they
  score `0` in this format.
- **SofaScore** (`points["2"]`): `sofascore` rating row (e.g. "6.9
  SofaScore · +4"), then a row per goal (same position-scaled bonus as
  AS), per penalty goal, per assist ("1 Asistencia · +1"). No row for
  cards — a red card's effect is already priced into a lower rating,
  yellow cards don't affect it at all.
- **Media**: `points["5"]`, shown as the literal formula
  ("Media: (6 + 4) / 2 · 5 puntos").

Explicitly out of scope for this slice (real mechanics, seen in the
screenshots, but with no known way to derive them from this API — see
`docs/biwenger-api-notes.md`): "Súper Pica" (a `+3` AS-only bonus, cause
unknown) and the "MVP" badge. Don't guess at either; leave them off
rather than showing something wrong.

Backing data confirmed available: the same per-gameweek `reports` entry
behind `view-player-performance-history`
(`docs/biwenger-api-notes.md` § "Per-gameweek points via `reports`"),
once fetched with more fields than that feature currently asks for:
- `match.round.name`/`.short` (match day), `match.date` (kickoff unix
  timestamp), `match.home`/`match.away` (`id`, `name`, `score` each).
- `rawStats.picas`, `rawStats.sofascore` — the two base ratings.
- `rawStats.goals`, `.goalsPenalty`, `.assists`, `.yellowCard`,
  `.redCard`, `.secondYellowCard` — bonus event counts (`.ownGoals`,
  `.penaltyMissed` also exist, unused so far, no samples seen).
- `events`: `{type, period, metadata}` array, `metadata` being the match
  minute — needed for the substituted-on/off rows (`type: 4`/`5`), not
  for anything points-related.
- The full reverse-engineered scoring formula (base-rating tables,
  per-bonus point values, the AS+SofaScore averaging formula) is in
  `docs/biwenger-api-notes.md` § "Diario AS / SofaScore scoring formula
  (reverse-engineered)" — empirically verified against ~800 reports and
  cross-checked against 5 real-app screenshots, no mismatches. Not
  exposed by any endpoint field; has to be computed by this feature
  itself from the raw counts above, not read off `points[format]`
  (which only ever has each format's *total*, never a breakdown).

Sliced per `docs/ways-of-working/vertical-slicing.md` into six backend
increments (each a full-value tap-target on its own, Android side tracked
separately in `biwenger-client-android`, not this repo):

1. Header only — which match, when, the score. **Shipped**: `GET
   /players/{playerId}/match-day-details?matchDay=N&season=current|previous`
   (`src/match-day-details-api-handler.js`,
   `src/match-day-details-view.js`). No points yet.
2. Add the single points total (`points["5"]`, matching the existing
   chart). **Shipped**, same endpoint/files as slice 1.
3. Add the Diario AS block (picas row only). **Shipped**
   (`src/as-score.js` for the picas-base table).
4. Add the remaining AS bonus rows (goal/penalty/red). **Shipped**,
   including a correction found while building it: `rawStats.redCard`
   (-6) and `rawStats.secondYellowCard` (-3) turned out to be genuinely
   different penalties, not the same value as originally documented —
   see `docs/biwenger-api-notes.md`.
5. Add the SofaScore block (rating + goal/penalty/assist rows).
   **Shipped** (`src/sofascore-score.js`; `goalBonus` extracted to
   `src/goal-bonus.js` now that AS and SofaScore both need it — second
   use case).
6. Replace the flat total from slice 2 with the "Media: (AS + SofaScore)
   / 2" row. **Shipped** (`src/media-score.js`) — the top-level `points`
   field from slices 1-2 is gone, replaced by `media` (the computed
   average) alongside `as.points`/`sofaScore.points` for the two
   operands.

7. Substituted-on/off rows (`events` type `4`/`5` + minute). **Shipped**
   (`src/substitutions.js`) — a `substitutions` array on the response,
   one entry per event, since a player can in principle be subbed on and
   later off within the same match.

All seven backend slices are shipped. The Android side
(`biwenger-client-android`, not this repo, tracked here since it's the
same feature) is being built through its own matching slices:

- Android slice 1 — tapping a bar opens a new exclusive screen (same
  pattern as `PlayerDetailScreen`) showing the header only: home/away
  crest, name, score, match day, kickoff date. **Shipped**
  (`biwenger-client-android` commit `0314fb0`).
- Remaining Android slices: points total, AS block, SofaScore block,
  Media row, substitution rows — same order as the backend slices above.

Move to `done` once the Android side reaches parity with everything the
backend already exposes.
