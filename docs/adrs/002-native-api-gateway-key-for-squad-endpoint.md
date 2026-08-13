# 002. Native API Gateway Key for the Squad Endpoint

## Status

Accepted

## Context

The squad endpoint had no access control (see
`docs/concerns/unprotected-squad-endpoint.md`): any caller with the URL
could trigger a real Biwenger login and read the account's squad. Fixing
this needed a caller that can attach a credential to every request — a
bare browser URL can't attach a custom header, which is why the fix
waited for a real client (the native Android app) to exist.

interest-tracker protects its endpoints the same way (its ADR-001): AWS
API Gateway native API keys (`private: true` + a usage plan), validated
via the `x-api-key` header, checked entirely at the gateway.

## Decision

Switch the squad endpoint from an `httpApi` (API Gateway v2, HTTP API)
event to an `http` (API Gateway v1, REST API) event with `private: true`,
backed by a usage plan and one API key (`biwenger-client-production-mobile-key`,
value from the `BIWENGER_CLIENT_MOBILE_API_KEY` GitHub secret). Native API
keys are a REST-API-only mechanism — this is also why the switch from
`httpApi` to `http` was necessary, not just the `private` flag.

The endpoint now also returns JSON (`{ players: [...] }`) instead of
server-rendered HTML, since its only consumer going forward is the native
app, not a browser. The old open HTML page and its handler
(`squad-handler.js`, `render-squad-page.js`) are retired.

## Alternatives Considered

**Passcode/session-token scheme of our own** — rejected. Would duplicate
what API Gateway already does for free, with more code to maintain and
test.

**Lambda Authorizer** — rejected, same reasoning as interest-tracker's
ADR-001: appropriate for dynamic authorization logic, not a static shared
key. Adds cold-start latency and cost with no benefit here.

**Unguessable secret path/URL** — considered while no real client existed
yet (see the concern doc). Rejected once the native app was decided on:
it's a weaker mechanism than a real key that can be rotated independently
of the URL, and the app can now hold a real key.

## Consequences

**Positive**
- Unauthorized requests are rejected at API Gateway; the Lambda is never
  invoked or billed for them.
- Zero application code change for the enforcement itself — pure
  `serverless.yml` configuration.
- Matches interest-tracker's mechanism, so the same operational knowledge
  (rotate via GitHub secret + redeploy) transfers directly.

**Trade-offs**
- AWS-specific; not portable without reconfiguration.
- The endpoint is no longer directly openable in a browser — the native
  app (or any client that can send `x-api-key`) is now required. That's
  the intended outcome, not a side effect.
- Key rotation is manual: update the GitHub secret, then redeploy.
