View the players I currently have listed on the market — asking price
and expiry, same shape `view-current-market` already shows for other
managers' listings. `enrich-squad-player-cards` already surfaces a
boolean "Listed" badge per squad player (`GET /market`'s `data.sales[]`
filtered to the requester's own `sale.user.id`, see
`docs/biwenger-api-notes.md` § "Squad player status"), but that's just a
flag; `getCurrentMarket()` actively excludes the requester's own sales
(it's "what can I bid on"), so this needs its own query over the same
`data.sales[]`, kept rather than filtered out this time.
