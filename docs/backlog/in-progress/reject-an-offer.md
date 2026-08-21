Reject an incoming offer, from the Offers subtab
(`view-offers-on-my-players`). First write action on this subtab — read-only
until now, same as the rest of Market before `place-a-bid`.

Needs a real write endpoint discovered/verified first (rejecting an
offer isn't in `docs/biwenger-api-notes.md` yet — only the read side,
`data.offers[]`, is documented) — same RAT-style verification
`saveLineup`/"Starting lineup — write" went through before being built,
not assumed from a reference project without checking against the real
API.

UI, on each `PlayerOfferRow`:

- A round red button with an "x", overlaid bottom-right of the card
  (same corner `PlayerAvatarWithPoints`' points badge sits in, on the
  opposite side).
- Tapping it opens a confirmation dialog: player photo + team crest,
  name, market value, the offer amount, and the difference between the
  two — everything stacked vertically except the photo/crest row,
  same visual language as the rest of the app (card surface, spacing,
  `priceTrend` coloring for the difference).
- Two buttons: gray "Cancel" on the left, red "Reject" on the right.
  Cancel closes the dialog and does nothing.
- Reject calls the write endpoint; while in flight, the "Reject" button
  swaps its text for a spinner (same button, not a separate overlay) so
  there's feedback and no double-submit. On success, close the dialog
  and reload the Offers list so the rejected offer disappears.
