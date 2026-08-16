Tapping a bar on the "Player performance" chart (from
`view-player-performance-history`) opens a detail view for that match
day. That feature shipped without a tap interaction at all (its
mockup's tap-to-tooltip was dropped, not deferred), so this is the tap
handler's first implementation, not a replacement for one.

Header:
- Home crest — score — away crest (`match.home`/`match.away`, each with
  `id`/`name`/`score`; crest built from the team id per the Image CDN
  convention already used for players).
- Below the crests, the match day and kickoff: "Match day 8 | Mar 10
  (Tue) - 13:00" (`match.round` for the match day number, `match.date`
  — a unix timestamp — for the formatted date/time).

Body:
- Points calculation (Diario AS) + bonuses breakdown. The existing
  performance-history endpoint already sums `points["5"]` for the bars,
  presumed to be the Diario AS format (unconfirmed — worth verifying
  which `points` key/label Biwenger itself attributes to Diario AS before
  building this).
- Substituted off: icon + the minute (e.g. "70'").
- Substituted on: icon + the minute entered.
- Goal(s): ball icon + "x2" (etc. — omitted for a single goal) + the
  bonus points for it.
- Assist(s): boot icon + "x2" (etc. — omitted for a single assist) + the
  bonus points for it.
- Booking(s): one icon per card received, coloured per card, each with
  the points deducted.

Backing data confirmed available: the same per-gameweek `reports` entry
behind `view-player-performance-history`
(`docs/biwenger-api-notes.md` § "Per-gameweek points via `reports`")
already carries everything this needs, once fetched with more fields
than that feature currently asks for:
- `match.round.name`/`.short` (match day), `match.date` (kickoff unix
  timestamp), `match.home`/`match.away` (`id`, `name`, `score` each).
- `events`: an array of `{type, period, metadata}`, `metadata` being the
  match minute. Types decoded empirically (2026-08-16, curling
  `GET .../players/la-liga/{id}?fields=id,name,reports&season={season}`
  for ~65 players and cross-checking against `rawStats`): `1`=goal,
  `2`=penalty goal, `3`=assist, `4`=substituted off, `5`=substituted on,
  `6`=yellow card, `7`=red card (including a second-yellow red, seen
  paired with a `6` in the same report). Types `8`–`14`, `16` also occur
  but weren't decoded — not needed for this slice. A report can have
  multiple events (e.g. a goal + a card).
- `rawStats` corroborates the above per-report (`goals`, `goalsPenalty`,
  `assists`, `yellowCard`, `redCard`, `minutesPlayed`) and is a simpler
  source for "how many" than counting `events` entries, though it won't
  give per-event bonus points — those presumably come from `points`
  broken out by category rather than a single total, which hasn't been
  explored yet (the endpoint call above only ever requested the summed
  `points` field, not a breakdown). Needs a request with more fields
  (e.g. `points` sub-breakdown or a scoring-rules lookup) to confirm
  where per-event bonus values actually live before implementing the
  bonus-points display.
