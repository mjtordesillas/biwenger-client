Reject an incoming offer, from the Offers subtab
(`view-offers-on-my-players`). First write action on this subtab —
read-only until now, same as the rest of Market before `place-a-bid`.

Write endpoint discovered/verified before building against it, RAT-style
(same as `saveLineup`): `PUT
https://biwenger.as.com/api/v2/offers/{offerId}` with
`{"status":"rejected"}`, verified empirically against a real incoming
offer — see `docs/biwenger-api-notes.md` § "Incoming offers — write".

**Backend**: `rejectOffer()` (`biwenger-client.js`) calls that endpoint.
New `reject-player-offer-api-handler.js`, wired to `PUT
/market/offers/{offerId}` — a private write proxy that collapses upstream
error details so a response can never disclose credentials, same
pattern as `saveLineup`'s handler.

**Android**: on each `PlayerOfferRow`, a small round button (tinted
low-alpha `TrendDown` background behind a full-opacity `TrendDown` "x"
glyph — the same color schema as the squad screen's "offer below market
value" status icon) overlaid bottom-right of the card, sized and padded
to clear the card's own corner radius so it isn't clipped. Tapping it
(the whole circle is the tap target, not just the glyph) opens a
confirmation dialog on one card surface: player photo/crest/name
centered, then a label-left/value-right quantities table (market value,
offer, and the difference with `priceTrend` coloring), then pill-shaped
Cancel/Reject buttons (same tinted-background/full-opacity-text
treatment, purple for Cancel — Nocturne's `ColorAccent`/primary — red
`TrendDown` for Reject) laid out with Cancel and Reject at opposite ends
of the row. Reject calls `MarketService.rejectOffer`; while in flight,
the button swaps its text for a spinner (no double-submit). On success,
the dialog closes and the Offers list reloads so the rejected offer
disappears.

Verified on a real device: the reject button opens the dialog with the
right player/amounts, Reject removes the offer from the list, Cancel
dismisses without side effects.
