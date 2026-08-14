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
