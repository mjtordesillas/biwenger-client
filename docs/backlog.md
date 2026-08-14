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

## Done

- View my squad — shipped; see `docs/rat.md` for the RAT behind it.
- Access my data in a secure manner — squad endpoint protected by a
  native API Gateway key (`docs/adrs/002-native-api-gateway-key-for-squad-endpoint.md`),
  consumed by a native Android app (`biwenger-client-android`) that holds
  and sends it as `x-api-key`. Verified working end to end on a physical
  device.
