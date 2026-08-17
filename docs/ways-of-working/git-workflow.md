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

## Deploy

`backend/` deploys continuously: CI (`.github/workflows/backend-ci.yaml`)
runs tests then `serverless deploy` on every push to `main` touching
`backend/**`. `android/` does not — it's a personal app with no
distribution channel worth automating, installed manually via `make
install` (build, test, `adb install`) from a machine with the phone
connected over USB. A slice spanning both subdirectories still lands as
one commit to `main`; the backend half goes live immediately, the
Android half goes live the next time you run `make install`.
