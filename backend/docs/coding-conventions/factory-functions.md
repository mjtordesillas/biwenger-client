# Factory Functions and Dependency Injection

Services are created via factory functions that accept an optional
dependencies object with defaults.

```js
// avoid — positional dependencies, no defaults, hard to test
const createBiwengerClient = (httpClient, tokenStore) => { ... }

// prefer — named deps with defaults, easy to override in tests
export const createBiwengerClient = (dependencies = {}) => {
    const {
        httpClient = defaultHttpClient,
        tokenStore = defaultTokenStore,
    } = dependencies

    // ...
    return { login, getSquad, getMarket }
}
```

Applied uniformly across all layers:

- Domain/use-case: `createSquadService({ biwengerClient })`
- Handlers: `createSquadHandler({ squadService })`
- Infrastructure: `createBiwengerHttpClient({ baseUrl, fetch })`

**In tests**, override only the deps you care about:

```js
const service = createSquadService({ biwengerClient: fakeBiwengerClient })
```

**Why:** factories make dependencies explicit and swappable without a DI
framework. Defaults mean production wiring requires no arguments; tests
inject fakes for only the collaborators under test.

*(Ported from interest-tracker. Applies once there's a real dependency to
inject — Slice 1's single handler may not need this until a second
collaborator, e.g. a cache or a second API client, shows up.)*
