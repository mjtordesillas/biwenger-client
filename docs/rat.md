# RAT — Can we authenticate against Biwenger and retrieve the squad?

**Status: PASSED** (2026-08-13)

## Question

Can we authenticate against the current Biwenger infrastructure and
retrieve the authenticated user's squad using reproducible HTTP requests —
enough to display player name, position, and market value?

## Method

Reverse-engineered current endpoints/headers from three reference projects
(not assumed correct, verified empirically):

- [FantasyManager](https://github.com/alexgasconn/FantasyManager) — `api/biwenger/*.ts`
- [pybiwenger](https://github.com/pablominue/pybiwenger) — `client/client.py`, `client/urls.py`
- [biwenger-transfers](https://github.com/jbujalance/biwenger-transfers) — bearer-token pattern, transfer board endpoint

Then ran [`scripts/rat-biwenger.sh`](../scripts/rat-biwenger.sh) against the
real API with real credentials, locally, outside of any chat/log.

## Endpoints confirmed live

1. `POST https://biwenger.as.com/api/v2/auth/login`
   Body: `{"email": ..., "password": ...}` → `{"token": "<JWT-like string>"}`

2. `GET https://biwenger.as.com/api/v2/account`
   Header: `Authorization: Bearer <token>`
   → `data.leagues[0].id` (use as `X-League`), `data.leagues[0].user.id` (use as `X-User`)

3. `GET https://biwenger.as.com/api/v2/user?fields=players(id,owner)`
   Headers: `Authorization`, `X-League`, `X-User`
   → `data.players[]` = list of `{id, owner}` — player IDs owned by the
   current user, **no name/position/price on this endpoint**.

4. `GET https://biwenger.as.com/api/v2/competitions/la-liga/data?lang=es&score=5`
   No auth required.
   → `data.players` keyed by player ID: `{id, name, position, price, ...}`.
   Join against the IDs from step 3 to get displayable squad data.

## Result

Real run against a real account: login succeeded, account resolved to a
league + user id, squad returned 14 players, and the catalogue join
resolved name/position/price for each, e.g.
`{id: 15396, name: "Brugué", position: 4, price: 280000}`.

`position` is an integer code, presumed `1=GK 2=DF 3=MF 4=FW` (consistent
with the one sample checked; worth confirming against a couple more
players before hardcoding a label map).

## Open questions / risks not yet resolved

- **Session lifetime is unknown.** Not tested. Re-run the script after
  several hours/days to see if the token still works, or a fresh login is
  required per use. For Slice 1 we're planning to log in fresh on every
  request anyway, so this mostly matters for later slices that might want
  to cache a session.
- Login here used a Biwenger password explicitly set for this account.
  Accounts that only ever used "Sign in with Google" and never set a
  password would need to do that first — the `/auth/login` endpoint in all
  three reference projects expects email+password, not an OAuth token.
- These are unofficial, undocumented endpoints. No SLA, no changelog. They
  can change without notice — acceptable risk for this project's scope.

## Conclusion

Option 1 (current Biwenger HTTP API) is sufficient. No need for browser
automation or PCAP-based discovery for Slice 1.
