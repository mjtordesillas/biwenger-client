# Named Parameters

All functions accept a single destructured object instead of positional
parameters.

```js
// avoid
const placeBid = async (playerId, amount, leagueId) => { ... }
placeBid(15396, 5000000, 658268)

// prefer
const placeBid = async ({ playerId, amount, leagueId }) => { ... }
placeBid({ playerId: 15396, amount: 5000000, leagueId: 658268 })
```

Applied uniformly across all layers:

- Factory functions: `createBiwengerClient({ httpClient, token })`
- Domain/use-case functions: `market.placeBid({ playerId, amount })`
- Handler factories: `createSquadHandler({ biwengerClient })`
- Infrastructure clients: `biwengerClient.fetchSquad({ leagueId, userId })`

**Why:** call sites are self-documenting, parameters can be added or given
defaults without breaking existing callers, and argument order stops
mattering.

*(Ported from interest-tracker.)*
