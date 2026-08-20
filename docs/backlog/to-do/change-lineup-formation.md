Change the active formation (e.g. "3-5-2" to "4-4-2"), from the pitch
view `view-my-lineup` ships. Needs the same write path against Biwenger
as `swap-lineup-players` (`PUT /user?fields=*` per
`docs/biwenger-api-notes.md`, unverified) plus a save/confirm flow, but
is a separate slice: picking a new formation without also resolving
which eleven fill its slots is a smaller, independently shippable
change than swapping players.
