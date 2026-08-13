# Share Behavior Through Modeling

If this codebase grows to model something with multiple variants (e.g.
squad players by position, market entries by status), it's easy to give
each variant its own file that looks self-contained and correct on its
own — and just as easy to end up deriving the same thing independently in
several of those files.

Before writing logic in one variant's file that derives something from
data the aggregate already carries, check whether a sibling file needs the
same derived result. If it does — or plausibly will — that's a sign the
underlying data hasn't been fully modeled yet: it's still being read and
interpreted ad hoc wherever it's needed, rather than represented as
something with its own behavior.

**Apply the rule of three.** The same derivation appearing in a second
file is a coincidence worth noting but not necessarily acting on yet — two
data points aren't a pattern. A third occurrence is the signal to stop and
model: at that point it's no longer plausible that each file arrived at
the same logic independently by chance.

```js
// two positions already derive this the same way; a third is about to
// add a third copy:
const derivedField = (rawPlayer) => { ... }   // <- stop here; occurrence #3

// model the concept instead, and have all variants compose it:
const player = createPlayer(rawPlayer)
player.derivedField()
```

Where variants genuinely need different outcomes from the same data,
express that as a parameter or method variant on the model, not as
separate logic per file. The model becomes the one place that behavior is
defined; every variant that uses it is just a caller.

This is a judgment call made at the point of writing new variant logic,
not a rule with a single automatic trigger.

*(Ported from interest-tracker. Not applicable yet — Slice 1 has no
per-variant files to compare. Kept here so the pattern is recognized
early, once player/market variants start accumulating.)*
