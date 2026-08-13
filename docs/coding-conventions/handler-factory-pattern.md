# Handler Factory Pattern

Lambda handlers are created via factory functions, not defined inline.

```js
// avoid — handler defined inline, dependencies hardcoded
export const handler = async (event) => {
    const client = createBiwengerClient()
    // ...
}

// prefer — factory injects dependencies, returns the handler function
export const createSquadHandler = ({ biwengerClient = createBiwengerClient() } = {}) => {
    return async (event) => {
        const squad = await biwengerClient.getSquad()
        return { statusCode: 200, headers: { 'Content-Type': 'text/html' }, body: renderSquad(squad) }
    }
}

// wired up as a singleton in index.js
export const squad = createSquadHandler()
```

Each handler directory has an `index.js`/`index.ts` that:
1. Imports each factory
2. Calls it with production dependencies (or defaults)
3. Exports the resulting handler for Serverless Framework to bind to a
   Lambda

**Why:** factories make handlers testable — tests call the factory with
fakes and invoke the returned function directly, no Lambda runtime needed.
Production wiring stays in `index.js`, separate from logic.

*(Ported from interest-tracker. With a single Slice-1 handler this is
one small factory, not yet a directory-per-consumer structure — grow the
structure only when a second handler needs it.)*
