Accept an incoming offer, from the Offers subtab
(`view-offers-on-my-players`). Sibling to `reject-an-offer`, same subtab,
same read-only-until-now context.

Needs a real write endpoint discovered/verified first, same RAT-style
verification as `reject-an-offer`'s — check the real API before
building against it, don't assume from a reference project. Unlike
`reject-an-offer`, this one is **not verified live against the API**:
accepting is irreversible (there's no undo, unlike a reject you could in
principle re-offer), so the RAT/verification step waits until there's a
real offer worth actually accepting — no throwaway test run against a
live squad player. Build and ship the UI/write-call plumbing against the
documented shape first; verify for real the next time an offer worth
accepting shows up.

UI, on each `PlayerOfferRow` — same shape as `reject-an-offer`'s button
and dialog, mirrored:

- A round green button with a check mark, overlaid bottom-right of the
  card, alongside (not replacing) the reject button — same tinted
  low-alpha background / full-opacity glyph color schema, but green:
  `PositionColors[3]` (MF's color), matching the reject button's use of
  `TrendDown`.
- Tapping it opens the same confirmation dialog design as
  `reject-an-offer` (player card surface, photo/crest + name, then the
  market value / offer / difference table), with an "Accept" button
  where Reject's was — same tinted-background/full-opacity-text pill
  treatment, in the same green as the button on the card.
- Cancel behaves identically (purple, opposite end of the row).
- Accept calls the write endpoint; same in-flight spinner-in-button
  behavior as Reject. On success, close the dialog and reload the Offers
  list so the accepted offer disappears.
