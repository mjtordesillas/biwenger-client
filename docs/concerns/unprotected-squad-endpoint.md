# Squad Endpoint Has No Access Protection

The Slice 1 squad page (`GET /` on the deployed HTTP API) has no
authentication or access control. Anyone who obtains the URL can trigger
it, and each hit performs a real login to the configured Biwenger account
and returns that account's squad.

## Current Evidence

- `serverless.yml`'s `squad` function is wired via a plain `httpApi` event
  with no `private`/authorizer/API-key configuration.
- `src/squad-handler.js` always calls `biwengerClient.getMySquad(credentials)`
  with the server-side Biwenger credentials, unconditionally, for every
  request — no caller identity check at all.

## Why This Is a Problem

interest-tracker protects its endpoints with AWS API Gateway native API
keys (`private: true` + a usage plan; see its ADR-001). That mechanism
only validates the `x-api-key` **header** — there's no query-string
equivalent — which assumes a caller that can attach custom headers: an
app or a JS frontend. Slice 1's product model is "open a URL directly in
a mobile browser," with no app and no frontend JS layer to attach
anything. A browser navigating to a bookmarked/typed URL cannot send a
custom header, so the interest-tracker mechanism doesn't transfer directly
without also introducing a Lambda authorizer or some other query-string
based scheme.

## Implications

- Anyone with the URL can repeatedly trigger real Biwenger logins on this
  account — cost/rate-limit risk against Biwenger, and AWS invocation
  cost, from anyone who finds or guesses the URL.
- The squad/market-value data returned is personal financial data, exposed
  to anyone with the URL, not just the account owner.
- This is currently accepted as low-probability risk: the URL isn't
  published or indexed anywhere. That acceptance should not be assumed to
  hold as more slices (and more surface area) are added.

## Improvement Proposal

The real fix likely isn't "bolt a header/query key onto a server-rendered
page" — it's that a bare server-rendered HTML endpoint is the wrong shape
once real access control matters. A PWA or small native/app shell would
have somewhere to hold a token (local storage, secure storage, a login
screen) and could then attach it as a header on every request, which is
exactly the model interest-tracker's native-API-key mechanism assumes.
That's a bigger step than Slice 1 warranted, so it wasn't taken here.

If protection is needed sooner than a PWA/app decision, the cheapest
interim options (in rough order of effort) are: an unguessable secret path
segment (capability URL, no AWS infra), or a Lambda authorizer that reads
a query-string token (closer to a real access-control mechanism, more
moving parts).

## Migration Approach

Revisit when either (a) the URL's exposure risk stops feeling acceptable,
or (b) a slice that needs real user identity/auth anyway (e.g. placing
bids, viewing rivals) forces the frontend-shape question regardless.
