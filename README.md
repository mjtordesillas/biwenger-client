# Biwenger Client

A small, fast, ad-free alternative client for [Biwenger](https://biwenger.as.com).

Not a rebuild of Biwenger. A thin client that exposes only the parts of our
league we actually use, built as a sequence of tiny, independently
deployable vertical slices (Lean/Agile, Trunk-Based Development, Elephant
Carpaccio — see `docs/`).

## Status

**Riskiest Assumption Test (RAT): PASSED.**

We confirmed we can authenticate against the current Biwenger v2 HTTP API
with email/password and retrieve the authenticated user's squad (player
name, position, market value) using 4 reproducible HTTP calls. Details and
the experiment script are in [`docs/rat.md`](docs/rat.md).

Currently building **Slice 1**: open a URL, see your current squad, no ads.

## Principles

- Every commit to `main` should be deployable.
- Every slice is a complete, tiny piece of user value — not a layer of
  architecture.
- No abstractions before the second use case demands them.
- Read-only first. No automated bidding. No credentials in source control.

See [`AGENT.md`](AGENT.md) for working conventions.

## First-time setup

### Environment files

- `.env` — gitignored, local only. Real secrets: `BIWENGER_EMAIL`, `BIWENGER_PASSWORD`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`.
- `.env.production` — committed. Non-secret config only (currently empty — nothing non-secret to configure yet).

Never put a secret in `.env.production`.

### Local development

```sh
npm ci
npm test
```

To deploy manually from your machine, create `.env` with the secrets above, then:

```sh
set -a; source .env; set +a
npm run deploy
```

### GitHub Actions secrets

Add these in **Settings → Secrets and variables → Actions** (repository
secrets, or under a `production` environment if you want an approval gate
later):

| Secret | Description |
|--------|--------------|
| `BIWENGER_EMAIL` | Biwenger account email used server-side to fetch the squad |
| `BIWENGER_PASSWORD` | Biwenger account password |
| `AWS_ACCESS_KEY_ID` | AWS credentials used by `serverless deploy` |
| `AWS_SECRET_ACCESS_KEY` | AWS credentials used by `serverless deploy` |
| `SERVERLESS_ACCESS_KEY` | Serverless Framework v4 access key (non-interactive auth for CI) |

Currently using root AWS credentials for deploys, by explicit choice, while
this is a single-service personal project — see
`docs/concerns/root-aws-credentials-for-ci-deploys.md`. Revisit before this
account holds anything else of value.

Push to `main` after these are set and CI will run tests then deploy.

## Repo layout (evolves as slices are added)

```
docs/
  rat.md                 RAT write-up
  adrs/                   Architecture Decision Records (Nygard format)
  concerns/               deferred design/workflow issues (empty for now)
  ways-of-working/        git workflow, concerns guide
scripts/                  small experiment/ops scripts (e.g. the RAT script)
```
