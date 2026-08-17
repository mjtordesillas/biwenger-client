# IIFE Singletons

Infrastructure services that have no external dependencies and need only
one instance use the IIFE (Immediately Invoked Function Expression)
pattern.

```js
// avoid — exported mutable state or class instantiation
export const clock = new Clock()

// prefer — IIFE encapsulates private state, exports a frozen interface
export const clock = (function IIFE() {
    const now = () => new Date().toISOString()
    return { now }
})()
```

Use IIFE singletons for stateless, dependency-free providers such as:
- `clock` — current date/time provider
- `consoleLogger` — logging wrapper

Do **not** use IIFE for services that need injected dependencies — use a
[factory function](./factory-functions.md) instead.

**Why:** the IIFE keeps internal helpers private, signals that exactly one
instance is intended, and avoids the overhead of a class while still being
testable by swapping the export in tests via module mocking.

*(Ported from interest-tracker. Not exercised yet — Slice 1 has no
dependency-free singleton to model this way.)*
