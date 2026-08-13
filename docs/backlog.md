# Backlog

Candidate features, not a roadmap. Each one is a complete, thin vertical
slice — deployable to production on its own, delivering real user value —
not a layer of architecture. No ordering is implied; picking the next one
should be driven by actually using the current slice in production, not
by this list's order.

- View the current market
- View a player in the market and place a bid
- View/edit my lineup
- View league standings
- View league movements/transfers
- View a rival's squad
- View useful rival context (balance, positional needs) where available
- Add price history/trends
- Add recommendations
- Access my data in a secure manner — the app itself has no access
  control right now (see `docs/concerns/unprotected-squad-endpoint.md`);
  likely resolved by a PWA/app decision rather than a header/query hack
  bolted onto a server-rendered page

## Done

- View my squad — shipped; see `docs/rat.md` for the RAT behind it.
