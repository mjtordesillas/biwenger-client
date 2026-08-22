Remove one of my own outgoing bids, from the Bids tab
(`getMyBidsOnOtherPlayers`) — same style as `unlist-a-player`: a small
round button on the player card, tinted low-alpha background behind a
full-opacity glyph, same color schema. No confirmation popup — tapping
the button removes that bid directly. While the request is in flight,
the glyph swaps for a spinner on that same button (no double-submit).
On completion, the Bids list reloads so the removed bid disappears.
Applies to a standing bid on a free agent too. Write endpoint not yet
confirmed against the real API (a bid is likely also an `offers/{id}`
entry, but removing my own outgoing bid may differ from rejecting an
incoming one) — verify RAT-style before building.
