View the players I currently have listed on the market — asking price
and expiry, same shape `view-current-market` already shows for other
managers' listings. `enrich-squad-player-cards` already surfaces a
boolean "Listed" badge per squad player (`GET /market`'s `data.sales[]`
filtered to the requester's own `sale.user.id`, see
`docs/biwenger-api-notes.md` § "Squad player status"), but that's just a
flag; `getCurrentMarket()` actively excludes the requester's own sales
(it's "what can I bid on"), so this needs its own query over the same
`data.sales[]`, kept rather than filtered out this time.

Lands as a subtab under the Market section, alongside
`view-offers-on-my-players` — same subtab-row pattern Squad's
Players/Lineup already uses (`SquadSubTab`/`SquadSubTabRow` in
`SquadScreen.kt`: an enum, a full-width equal-split tab bar with an
underline under the selection), ported to Market rather than invented
fresh. Whichever of these two ships first decides whether
`view-current-market`'s existing listings become the third subtab
alongside it, or stay the section's un-tabbed default with only these
two added — not decided yet, a call for whoever picks this up.
