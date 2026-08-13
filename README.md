# Biwenger Client

A small, fast, ad-free alternative client for [Biwenger](https://biwenger.as.com).

See [`AGENT.md`](AGENT.md) for ways of working and [`docs/backlog.md`](docs/backlog.md) for candidate features.

## Setup

### Environment files

- `.env` — gitignored, local only. Real secrets: `BIWENGER_EMAIL`, `BIWENGER_PASSWORD`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`.
- `.env.production` — committed. Non-secret config only (currently empty — nothing non-secret to configure yet).

Never put a secret in `.env.production`.

### GitHub Actions secrets

Add these in **Settings → Secrets and variables → Actions**:

| Secret | Description |
|--------|--------------|
| `BIWENGER_EMAIL` | Biwenger account email used server-side to fetch the squad |
| `BIWENGER_PASSWORD` | Biwenger account password |
| `AWS_ACCESS_KEY_ID` | AWS credentials used by `serverless deploy` |
| `AWS_SECRET_ACCESS_KEY` | AWS credentials used by `serverless deploy` |
| `SERVERLESS_ACCESS_KEY` | Serverless Framework v4 access key (non-interactive auth for CI) |

## Run

```sh
npm ci
npm test
```

## Deploy

Push to `main` with the secrets above set — GitHub Actions runs the tests,
then deploys on green.

To deploy manually from your machine instead, create `.env` with the
secrets above, then:

```sh
set -a; source .env; set +a
npm run deploy
```
