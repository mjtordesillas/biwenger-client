# Biwenger Client

A small, fast, ad-free alternative client for [Biwenger](https://biwenger.as.com).

See [`AGENT.md`](AGENT.md) for ways of working and [`docs/backlog/`](docs/backlog) for candidate features, organized by state.

## Setup

### Environment files

- `.env.example` — committed. Names every required secret, no values. Copy to `.env` and fill in for local use.
- `.env` — gitignored, local only. Real secret values.
- `.env.production` — committed. Non-secret config only (currently empty — nothing non-secret to configure yet).

Never put a secret in `.env.production`.

### GitHub Actions secrets

Add each name from `.env.example` as a repository secret in
**Settings → Secrets and variables → Actions**, with real values.

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
