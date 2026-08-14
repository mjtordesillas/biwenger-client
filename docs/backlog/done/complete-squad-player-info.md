Complete player information in my squad: current price, last
increment/decrement, snapshot picture URL, team crest picture URL, main
position/role, secondary position/role (sometimes empty), total points.

Shipped. Backend: `src/player-view.js` shapes each squad player with
`priceIncrement`, `points`, `secondaryPosition`, `photoUrl`, and
`teamCrestUrl` (image URLs built from CDN path conventions verified
empirically — see commit history). Android: `SquadScreen` renders photo,
team crest, price movement (colored), position/secondary position, and
points. Verified working end to end on a physical device.
