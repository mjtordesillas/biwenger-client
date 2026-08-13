# Testing Philosophy

Ported from interest-tracker's testing strategy. Interest-tracker earns
this pyramid through a real DB, multiple consumers, and event-driven
workers; this project doesn't have any of that yet. Treat the layers below
as the strategy to grow into as slices add real infrastructure — not a
checklist to satisfy before Slice 1 ships. Right now, a handful of narrow
unit tests around the parts worth distrusting (the squad/catalogue join,
header construction) is enough; add layers only when there's
infrastructure that specifically warrants them.

## Test Pyramid

```
          /\
         /  \
        / E2E\
       /------\
      /Security\
     /----------\
    / Integration\
   /--------------\
  /   Wide Unit    \
 /------------------\
/    Narrow Unit     \
\____________________/
```

The pyramid reflects the relative weight of each layer: narrow and wide
unit tests form the bulk of the suite; integration and security tests are
fewer but targeted; end-to-end tests are the smallest set, covering only
key user journeys.

---

## Layers

### Narrow Unit Tests

**What they are**: Tests that verify a single unit in strict isolation —
domain rules, shared contracts, and logic too intricate to validate
through a wider lens (complex mappings, edge-case algorithms — e.g. the
squad/catalogue join, or the position-code mapping).

**Cost**: Low. No infrastructure, instant feedback.

**Speed**: Instant.

**Confidence**: Narrow — confirms a specific rule or contract holds, not
that the system behaves correctly end-to-end.

**Role in the development lifecycle**:
- Can drive TDD at the unit level for tricky domain logic or infrastructure mappings.
- Narrow tests written *purely to support test-driven design* are temporary harnesses — delete once the design stabilises.
- The narrow tests that *remain* document and enforce a specific contract, a non-obvious rule, or a complex transformation.
- Run on every CI push; block deploy on failure.

---

### Wide Unit Tests

**What they are**: Tests that exercise a full slice — handler, use-case
logic, an in-memory fake for the external client — mocking only at the
ports (external HTTP clients). The primary outside-in TDD loop.

**Cost**: Low. No infrastructure, instant feedback.

**Speed**: Instant.

**Confidence**: Medium. Validates that a feature works end-to-end within
the application boundary, but does not verify real HTTP wire behaviour.

**Role in the development lifecycle**:
- Written test-first: the failing wide unit test is the starting point for every feature or change.
- The primary regression safety net for handler logic and application behaviour.
- Run on every CI push; block deploy on failure.

---

### Narrow Integration Tests

**What they are**: Tests that verify one real infrastructure component in
isolation — the Biwenger HTTP client's wire behaviour against a
recorded/mocked response, for example. Only the component under test is
real.

**Cost**: Medium. Requires network-level interception or recorded fixtures.

**Speed**: Slow relative to unit tests.

**Confidence**: Targeted but high for their scope.

**Role in the development lifecycle**:
- Run locally during active infrastructure work: implementing or changing the Biwenger client.
- Run on every CI push once they exist; block deploy on failure.

---

### End-to-End Tests

**What they are**: Tests that validate complete user journeys through the
full running stack, with no mocks anywhere. Requires real Biwenger
credentials, so likely stays a small, manually-triggered suite rather than
a CI gate, unless a disposable test account becomes available.

**Cost**: High.

**Speed**: Slowest.

**Confidence**: Highest.

**Role in the development lifecycle**:
- Written for key user journeys, not for exhaustive scenario coverage.
- Run locally before pushing when a change affects a specific flow.

---

### Security Tests

**What they are**: Tests that verify credential handling — e.g. that no
endpoint leaks the Biwenger token/password to the frontend or into logs.
Decoupled from functional test suites so this coverage cannot be silently
dropped once endpoints multiply.

**Cost**: Medium.

**Role in the development lifecycle**:
- Not part of the feature TDD loop. Written once there's more than one
  endpoint and a real risk of a leaky one; maintained as the endpoint
  list grows.

---

## Summary

| Layer | Cost | Speed | Confidence | CI gate | Local trigger |
|---|---|---|---|---|---|
| Narrow unit | Low | Instant | Narrow / precise | Every push | Before commit (discipline) |
| Wide unit | Low | Instant | Medium | Every push | Before commit (discipline) |
| Integration | Medium | Slow | High (infra scope) | Every push, once they exist | During infra work |
| End-to-end | High | Slowest | Highest | Manual for now | Before push on flow changes |
| Security | Medium | — | Complete for scope | Once endpoints multiply | Not typically local |
