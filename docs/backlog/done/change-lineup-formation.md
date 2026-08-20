Change the active formation (e.g. "3-5-2" to "4-4-2"), from the pitch
view `view-my-lineup` ships. Uses the same write path
`swap-lineup-players` already verified and shipped (`PUT
/user?fields=lineup(type,playersID)`, see `docs/biwenger-api-notes.md`
§ "Starting lineup — write") plus a save/confirm flow, but is a
separate slice: picking a new formation without also resolving which
eleven fill its slots is a smaller, independently shippable change than
swapping players.

**Free formations only** — confirmed against Biwenger's own picker UI
(not the API, which exposes no formations list): `3-4-3`, `3-5-2`,
`4-3-3`, `4-4-2`, `4-5-1`, `5-3-2`, `5-4-1`, in that order. Anything
outside this list is presumed a paid "extra" formation (Biwenger's own
public statement: extra formations cost credits even in free leagues,
capped at 3 forwards otherwise) — not verified against the write
endpoint, and deliberately excluded from this feature's UI entirely,
not just discouraged. See `docs/biwenger-api-notes.md` § "Starting
lineup — write" for the full note.

**Shipped** (2026-08-20, `0a67df2`): one slice, Android only — no
backend change needed. The formation label is a real dropdown
(`DropdownMenu`), listing the 7 free formations; picking one reshapes
the current eleven for the new band sizes (`reshapeLineup` — carries
players over where a band is unchanged or shrinking still fits them,
pads a growing band with vacant slots, drops overflow from a shrinking
band to the bench) and saves via the existing write path. A spinner
replaces the dropdown arrow while the write is in flight. Verified live
that switching within the free list costs no credits (21 credits
before and after a round trip).
