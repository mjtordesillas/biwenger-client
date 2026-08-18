See my starting lineup, sliced out of `edit-my-lineup` (viewing and
editing are two different slices — editing needs write access and a
save flow, viewing doesn't).

A new "Lineup" subtab alongside Squad's existing player list (renamed
"Players"), both under a subtab bar pinned to the top of the app, above
the screen content. Icon: a football-pitch outline.

The lineup subtab shows a green pitch (lines included) with the
starting eleven laid out by position — forwards nearest the top,
midfielders below them, defenders below those, goalkeeper at the
bottom — each player shown as their photo with their name underneath.
The active formation (e.g. "3-5-2") is visible above the pitch.

**Shipped** (2026-08-18): backend (`885a65b`) adds `GET /lineup`
(`data.lineup.type`/`playersID` off `/user`, joined against the
catalogue) — deployed and confirmed live; see
`docs/biwenger-api-notes.md` § "Starting lineup" for the formation
string shape and why `playersID`'s order didn't need parsing (each
player already carries its own `position`). Android (`3a7b233`) adds
the Players/Lineup subtab bar and the pitch view — `FootballPitch`
(`ui/`) draws the markings via Canvas, reused small as the tab icon and
large as the pitch background. Verified end-to-end against a real
account/league.
