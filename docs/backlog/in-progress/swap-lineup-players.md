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

Needs a write path against Biwenger (`PUT /user?fields=*` per
`docs/biwenger-api-notes.md`) and a save/confirm flow. Blocked on
`verify-lineup-write-endpoint` — don't start this until that spike has
confirmed the endpoint's actual shape.
