# 001. Use Biwenger's Unofficial v2 HTTP API Directly

## Status

Accepted

## Context

We need to read (and eventually write) data from a user's Biwenger account:
squad, market, lineup, standings. Biwenger has no official public API. Prior
art (FantasyManager, pybiwenger, biwenger-transfers) all reverse-engineer the
same undocumented `https://biwenger.as.com/api/v2/*` endpoints. Options
considered were: (1) call these endpoints directly over HTTPS, (2) drive the
official web app with browser automation (Playwright/Puppeteer), (3) rely on
PCAP/traffic inspection as an ongoing integration mechanism.

We ran a Riskiest Assumption Test (see `../rat.md`) against real endpoints
with a real account and confirmed: login, account, squad, and catalogue
(name/position/price) are all retrievable with 4 plain HTTP calls and a
Bearer token + `X-League`/`X-User` headers.

## Decision

Use the unofficial `biwenger.as.com/api/v2` HTTP API directly as the
integration mechanism, called server-side. Browser automation and
PCAP-based discovery are not used as the runtime integration path — PCAP
inspection remains available only as an ad-hoc discovery technique if an
endpoint needed later (bidding, lineup edits) isn't already documented by
prior art.

## Consequences

- Fast, simple, cheap to run on Lambda — no browser runtime needed.
- No SLA: Biwenger can change or break these endpoints without notice.
  Mitigation is cheap re-validation (rerun the RAT script) rather than
  defensive engineering up front.
- Credentials must be handled server-side only (never sent to/stored in the
  frontend), per the project's security principles.
- If a needed capability turns out not to exist in this API surface,
  browser automation becomes the documented fallback (not the default).
