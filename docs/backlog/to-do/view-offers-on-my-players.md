View incoming purchase offers on my own squad players — who's offering,
how much, and for which player. `enrich-squad-player-cards` already
surfaces a boolean "Offer received" badge per squad player from `GET
/market`'s `data.offers[]` (see `docs/biwenger-api-notes.md` § "Squad
player status" — `to.id`/`requestedPlayers` matching), but that's just a
flag; this is the actual detail (amount, offering party where
identifiable — `from` was `null` in every sample seen so far, worth
re-checking) behind it.
