# Concerns

Concerns document known design, architecture, workflow, or naming problems
that are worth preserving but not necessarily next to implement.

Use `docs/concerns/` when an issue is real, evidenced by the current
codebase or a real experiment (not speculation), and worth keeping before
deciding whether to fix it, turn it into a plan, record an ADR, or accept
it.

## When to create one

- a structural issue is discovered during unrelated work;
- fixing it now would make the current slice too large;
- the issue is explicitly deferred rather than forgotten.

Do not create one for a "consider improving this" note with no concrete
impact, or for something already covered by an ADR or this doc set.

## Shape

```markdown
# Short Concern Title

Brief summary of the problem.

## Current Evidence

Concrete paths, endpoints, or experiment results — not memory.

## Why This Is a Problem

## Implications

## Improvement Proposal
```

## Lifecycle

A concern should eventually become one of: **deferred** (stays as-is),
**a plan** (`docs/plans/...`), **an ADR** (`docs/adrs/...`), **resolved**
(removed), or **accepted** (updated to explain why it stays).
