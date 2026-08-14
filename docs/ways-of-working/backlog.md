# Backlog

Candidate features live as individual files under `docs/backlog/<state>/`,
one file per feature, organized by state:

- `to-do/` — not started
- `in-progress/` — actively being worked on right now
- `done/` — shipped and verified in production

This is a backlog, not a roadmap: no ordering within or across directories
implies priority or sequence. Which `to-do` file becomes `in-progress`
next is decided from actual production usage, not file order or
recency — see `docs/ways-of-working/vertical-slicing.md` for how thin the
resulting slice should be once picked.

## Moving a feature between states

A plain `git mv`, so the move is cheap and visible in git history/blame,
and "what's being worked on right now" is answerable by
`ls docs/backlog/in-progress/` alone:

```sh
git mv docs/backlog/to-do/<feature>.md docs/backlog/in-progress/<feature>.md
# ... later ...
git mv docs/backlog/in-progress/<feature>.md docs/backlog/done/<feature>.md
```

## File shape

Short and free-form — a paragraph is usually enough. A `done` file should
link to the evidence it actually shipped (an ADR, a RAT, a concern doc)
rather than restate it.
