Swap players in/out of the starting eleven, from the pitch view
`view-my-lineup` ships — same formation, different eleven. Includes
leaving a slot vacant (benching a starter with no replacement) — the
thinner half of "swap", since it needs no bench-eligibility logic, just
a shortened `playersID`; a full swap is remove-then-fill, not a
separate atomic operation. Excludes trading two players who are both
already starters (e.g. a starting DF for a starting MF): same-band is a
no-op (nothing renders intra-band order) and cross-band changes the
formation's shape, which belongs to `change-lineup-formation`, not
here.

**Shipped** (2026-08-20): three slices, backend+Android each time.

- Write-path spike (`ef7f38d`): confirmed
  `PUT /user?fields=lineup(type,playersID)` (see
  `docs/biwenger-api-notes.md` § "Starting lineup — write") — full
  fixed-length `playersID`, `null` for vacant, not a shortened array.
  The spike also caught a live bug in the already-shipped read side
  (`view-my-lineup` inferred vacancy from array length, which the real
  shape never produces) — fixed alongside (`ef7f38d`).
- Slice 1 (`8429ad8`): bench a starter, leaving the slot vacant — the
  app's first write path end to end.
- Slice 2 (`1a5ca88`): fill any slot (vacant or occupied) from an
  eligible same-position bench player, one dialog with a "Vacate"
  button for the occupied case instead of two separate steps.
- Slice 3 (`71373c9`): secondary-position eligibility ("Jollies", same
  primary/secondary split Biwenger's own editor uses) alongside
  same-position "Specialists". A second spike found off-position
  alignment costs 2 account-wide credits, deducted silently by
  Biwenger with no hint in the write response — an unaffordable jolly
  is disabled in the picker, not hidden.

Verified end-to-end against a real account/league throughout,
including the two credit-spending spikes themselves.
