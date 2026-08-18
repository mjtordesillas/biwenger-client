Enrich "My Squad" player cards with facts that matter for
squad-management decisions, beyond name/position/price/points: how many
days left until the transfer lock lifts and the player can be listed on
the market, whether it's currently listed on the market, whether there's
a standing offer for it, and its fitness status (injured / in doubt for
the next match). All five are already available from data Biwenger's API
exposes today — see `docs/biwenger-api-notes.md` § "Squad player status
(owner lock, market listing, offers, fitness)" for exactly where each one
comes from. Requested together, shipping together, same precedent as
`view-current-market.md`'s slice 3 (expiry/seller/market value).
