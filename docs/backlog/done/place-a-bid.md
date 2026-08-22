View a player in the market and place a bid.

Write endpoint RAT'd by capturing the real request live from Biwenger's
own web app via browser DevTools, verified 2026-08-22 against a real
free-agent listing (player `24956`, bid `150000`) — 200 with the
created offer echoed back. See `docs/biwenger-api-notes.md` §
"My outgoing bids — write (place)".

Unlike unlist/remove-bid, placing a bid commits real money in-game, so
per `AGENT.md`'s "no automated bidding/selling without explicit
confirmation" it goes through a confirmation dialog — same pattern as
`accept-an-offer`/`reject-an-offer`, not the no-dialog direct-tap
pattern. The bid amount is the app's first free-text input: an
editable field in the dialog, defaulted to the listing's asking price.

**Backend**: `placeBidData()`/`placeBid()` (`biwenger-client.js`), new
`place-bid-api-handler.js`, wired to `POST /market/my-bids` with a
`{"playerId": <playerId>, "amount": <amount>}` body — same `offers`
resource reject/accept/remove-bid already write to, this time a `POST`
to the collection rather than a verb on one existing entry. `playerId`
travels in the body rather than as a path segment (unlike list/unlist)
because API Gateway rejects two sibling resources under the same parent
with differently-named path variables: `DELETE
/market/my-bids/{offerId}` (remove-bid) already claims that slot, so
the initial `POST /market/my-bids/{playerId}` shape failed to deploy
(`ApiGatewayResourceMarketMyDashbidsPlayeridVar` — "a sibling
({offerId}) of this resource already has a variable path part"),
caught by CI on the first push. Same collapsed-upstream-error shape as
the other writes, covering a malformed body the same way
`save-lineup-api-handler.js` does.

**Android**: `HttpClient`/`RetrofitHttpClient` gained a body-carrying
`post` overload (every POST before this only needed the no-body one —
listPlayer's price and cycleListings' selection are both fixed
server-side). `MarketService`/`HttpMarketService` gained
`placeBid(playerId, amount)`. New `PlaceBidEffect`, same
request/finished shape as `RejectOfferEffect`'s. A "Place bid" button
(Gavel glyph, `AcceptGreen`, same tinted-background schema as the
other card buttons) on each Current Market row opens `PlaceBidDialog`
— same player-card/label-value-row/Cancel-action-button shape as
`PlayerOfferConfirmationDialog`, plus an `OutlinedTextField` for the
amount (defaulted to the asking price, digits-only, live "Difference"
vs. market value once valid). The amount itself is the dialog's own
`remember{}` state, not routed through the Registry — only the final
validated `Long` becomes the confirm event's payload, same reasoning
`MarketScreen`'s own `selectedSubTab` uses for ephemeral view-only
state. On completion the dialog closes and the Bids tab reloads, same
as `remove-a-bid`'s finished handler.

Verified live: the request/response shape above was captured directly
against a real listing via DevTools before writing any code.
