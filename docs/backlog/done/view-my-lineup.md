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

**Polish** (2026-08-18–20, `c2bea9d`..`94c2f67`): the pitch view's
player positioning went through several rounds — a plain mugshot
(dropping `PlayerAvatar`'s circle mask/team crest, redundant standing
on a pitch) in a pill name tag; rows of 3+ players bowed into a curve;
real penalty-area D-arcs and spots added to `FootballPitch`. Final
positioning (`8f52ab0`) was calibrated directly against a temporary
24-column x 20-row grid overlay (removed once done) rather than
guessed from prose — see that commit and the conversation around it
for the exact per-role row/column values. A vacant formation slot
(Biwenger's own lineup short of the formation's count) now renders a
"?" over Biwenger's default player photo (`i/p/0.png`, documented in
`docs/biwenger-api-notes.md` § "Image CDN") instead of silently
drawing fewer players than the formation says (`d38d40c`). The
Players/Lineup subtab bar itself was redone as a full-width underline
tab bar matching `PlayerDetailScreen`'s price-history tabs, on a
background a shade darker than the page (`ColorBgDeep`) to read as a
distinct nav layer (`05f7fa1`, `966d82b`, `d38d40c`).
