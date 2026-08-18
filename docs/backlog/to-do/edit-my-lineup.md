Edit my lineup — change the formation and swap players in/out of the
starting eleven, from the pitch view `view-my-lineup` ships. Needs a
write path against Biwenger (`PUT /user?fields=*` per
`docs/biwenger-api-notes.md`, unverified) and a save/confirm flow;
viewing is a separate, already-scoped slice.
