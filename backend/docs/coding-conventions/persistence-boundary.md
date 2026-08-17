# Persistence at the Boundary

Keep domain/data objects in their wrapped form through every
transformation. Convert to a plain, persistable/renderable shape exactly
once — at the point something is actually being stored, sent out, or
rendered — and let that conversion cascade through whatever the object
contains, rather than unwrapping mid-transformation and threading the raw
result back into another object.

```js
// avoid — unwraps mid-transformation, forcing the raw shape into the next object
const result = createSquadView({
    ...current.toPlain(),
    players: players.updated(item).toPlain(),
})

// prefer — stays wrapped through the transformation; unwrapping happens
// once, at the boundary, and cascades through whatever the object contains
const result = createSquadView({ ...current, players: players.updated(item) })
// ... later, only where the result is actually rendered or persisted:
result.toPlain()
```

**Why:** an object that only knows how to hold raw data internally forces
every collaborator that touches it to unwrap before handing data back, and
re-wrap before reading it again — conversion logic ends up scattered
across every transformation instead of living in one place. Converting
once, at the real boundary, means a change to the persisted/rendered shape
only has to be made where that conversion happens, not at every
mid-transformation call site that unwraps early.

*(Ported from interest-tracker. Not applicable yet — there is no
persistence layer, and Slice 1 has no wrapped domain object to unwrap
early. Keep this in mind once one exists.)*
