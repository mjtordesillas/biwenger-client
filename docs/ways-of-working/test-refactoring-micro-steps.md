# Test Refactoring in Micro-Steps

A how-to guide for safely refactoring test files one at a time, keeping
the suite green at every commit.

## When to use this

Use this pattern whenever you need to improve multiple test files that
share the same smell — for example, removing a duplicated fixture,
replacing indirect property references with literals, or migrating to a
new builder/helper. The files are independent of each other, so changes
can be applied one at a time with a green-bar checkpoint between each.

---

## The pattern

### 1. Plan before touching any code

Write a step-by-step plan that:

- Identifies each affected file as a separate step.
- Lists every sub-change within that step.
- States the expected post-condition: which test command to run and what "green" looks like.
- Includes a **precondition check** for anything that could go wrong silently.

Turn the plan into a **checklist** so progress is visible and nothing is
skipped.

### 2. For each step, work in this order

```
check preconditions
  → edit the test file
    → npm run test:unit          ← run immediately after finishing the edit
      → green: commit            ← one focused commit per file
      → red:   investigate       ← do not proceed to the next file
```

**Check preconditions first.** Verify any assumptions noted in the plan
before editing.

**Run tests as often as you like.** Running mid-edit is fine. The
constraint is on committing, not on running.

**Commit only when green.** Keep each commit scoped to a single file and a
single concern.

**Never proceed on red.** Diagnose the failure before touching the next
file.

### 3. Final verification

After all steps are committed, run the full suite once more and confirm:

- Every file in the plan has been changed.
- No leftover references to the patterns you were removing remain.
- The suite is green end-to-end.

*(Ported from interest-tracker. Not applicable yet — there is no test
suite. Kept here so the discipline is already documented once one exists.)*
