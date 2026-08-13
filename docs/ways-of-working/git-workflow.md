# Git Workflow

This project practices trunk-based development: commit and push directly to
`main`. Avoid feature branches — use one only for something genuinely risky
or long-running, and merge it back the same day.

`main` must stay deployable. Every commit should be a complete, working
slice — not a partial step toward one.

There is no formatting/lint pre-push hook yet because there is no
tooling to enforce (no linter/formatter/build configured). Add one in the
same slice that introduces the first build tool, rather than as a
separate step.
