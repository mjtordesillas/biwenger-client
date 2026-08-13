# Vertical Slicing (Elephant Carpaccio)

This is how we deliver whatever feature is chosen next. What to build is
picked from `docs/backlog.md` and actual production usage. This document
is about how thin the resulting slice should be.

## The rule

Cut every feature into the thinnest possible vertical slice that:

- delivers real, observable user value on its own;
- is complete end to end (UI/response, any backend logic, any external
  call, deployment);
- ships to production and gets used before the next slice is cut.

Elephant Carpaccio means slicing a feature into many thin, complete
cross-cuts, each one a full, working, shippable increment — rather than
building it in horizontal layers that only produce value once all of them
are done. Each slice should feel almost too small to be worth a commit.
If a slice feels big, cut it again.

## What this looks like in practice

**Horizontal** (value only at the end):
1. Build the market data client.
2. Build the market domain model.
3. Build the market page.
4. Wire it all together and ship.

**Vertical** (value at every step):
1. "I can open a URL and see the 5 most recent market listings, no
   styling." — ships, gets used, gets learned from.
2. "I can see all current market listings, not just 5." — ships.
3. "I can see each listing's expiry time." — ships.
4. ...and so on, only as far as real usage demands.

Every one of those is deployable and independently valuable on its own.

## Applying this to the current codebase

- No abstraction, layer, or generalization is added ahead of the slice
  that concretely needs it (see `AGENT.md`).
- A slice is ready to start once there's a concrete, small statement of
  the user-visible thing it delivers — if that statement needs "and" to
  describe it, it's probably two slices.
- Ship something narrower today over something more complete tomorrow.
