View incoming purchase offers on my own squad players — who's offering,
how much, and for which player. `enrich-squad-player-cards` already
surfaces a boolean "Offer received" badge per squad player from `GET
/market`'s `data.offers[]` (see `docs/biwenger-api-notes.md` § "Squad
player status" — `to.id`/`requestedPlayers` matching), but that's just a
flag; this is the actual detail (amount, offering party where
identifiable — `from` was `null` in every sample seen so far, worth
re-checking) behind it.

Lands as a subtab under the Market section, alongside
`view-my-market-listings` and `view-current-market`'s existing
listings — a sibling tab, not the default with only the two new ones
tabbed on top of it. Same subtab-row pattern Squad's Players/Lineup
already uses (`SquadSubTab`/`SquadSubTabRow` in `SquadScreen.kt`: an
enum, a full-width equal-split tab bar with an underline under the
selection), ported to Market rather than invented fresh — three
entries instead of two.
