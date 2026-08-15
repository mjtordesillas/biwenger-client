See a player's full information on a dedicated full-screen view, rather
than the current `PlayerDetailSheet` bottom sheet (`SquadScreen.kt` —
opened by tapping a player row on the squad list). What "full
information" covers and the exact layout are TBD — a mockup will be
provided when this is picked up.

Shipped. A mockup arrived showing the bottom sheet replaced by a
full-screen view plus a new team-name line under the avatar. The
team-name line needs its own backend join (Biwenger's catalogue endpoint
exposes `data.teams` keyed by `teamID`, not currently fetched) and is
deliberately out of scope here — this slice is the full-screen conversion
only, tracked as a separate follow-up.

Android: `PlayerDetailSheet` (a `ModalBottomSheet`) became
`PlayerDetailScreen`, rendered as an exclusive either/or with the squad
list (`if (selectedPlayer != null) PlayerDetailScreen(...) else <squad
list>`) rather than an overlay — an earlier overlay-based attempt left the
still-composed list underneath able to catch stray taps and swap to the
wrong player, since most of the detail screen had no pointer-input
handler of its own to claim them. The either/or structure removes the
list from composition entirely while the detail screen shows, fixing that
by construction rather than patching around it. Header now carries a `‹`
back button and the player's name as title; card colors flip to match the
mockup (dark screen background, elevated `ColorSurface` cards with a drop
shadow). No ViewModel/event/state changes — `selectedPlayerId` and
`priceHistory` still drive everything, and no navigation library was
needed. Verified via `./gradlew test` and manual exercise in the running
app.
