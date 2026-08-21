Accept an incoming offer, from the Offers subtab
(`view-offers-on-my-players`). Sibling to `reject-an-offer`, same subtab,
same read-only-until-now context.

Write endpoint built against the verified `reject-an-offer` endpoint's
documented shape (same `PUT .../offers/{offerId}`, only the `status`
value differs) — **not verified live against the API**: accepting is
irreversible, so this is deliberately left unverified until there's a
real offer worth actually accepting. See
`docs/biwenger-api-notes.md` § "Incoming offers — write" for the caveat,
kept in place until that real-world check happens.

**Backend**: `acceptOffer()` (`biwenger-client.js`), same shape as
`rejectOffer()` with `{"status":"accepted"}`. New
`accept-player-offer-api-handler.js`, wired to `PUT
/market/offers/{offerId}/accept` — a separate path from reject's (both
are PUTs against the same `{offerId}` resource, so they can't share one
route).

**Android**: `RejectOfferDialog` generalized into
`PlayerOfferConfirmationDialog` (title/action label/action color/
in-flight flag as parameters) — the second use case earning the
extraction, rather than a copy-pasted `AcceptOfferDialog`. On each
`PlayerOfferRow`, a green check button (`PositionColors[3]`, MF's
green — same tinted-background/full-opacity-glyph treatment as reject's
`TrendDown`) sits alongside (not replacing) the reject button, both in
a `Row` anchored bottom-right and padded clear of the card's corner
radius. Tapping it opens the same dialog design as reject, with "Accept
offer?" as the title and a green "Accept" pill button in place of red
"Reject". Same in-flight spinner-in-button behavior, full `MarketViewModel`
event/effect/state wiring mirroring reject's
(`offer-acceptance-opened/cancelled/requested/finished`,
`market.offerToAccept`/`market.acceptingOffer`).

Verified on a real device: the accept button opens the dialog with the
right player/amounts, and Cancel dismisses without side effects. Accept
itself has deliberately not been tapped for real — see the live-API
caveat above.
