# Naming Conventions

## Files

Use kebab-case for all file names.

```
// avoid
squadHandler.js
BiwengerClient.js

// prefer
squad-handler.js
biwenger-client.js
```

## Functions and variables

Use camelCase.

```js
// avoid
const get_squad = () => {}
const GetSquad = () => {}

// prefer
const getSquad = () => {}
const marketValue = 280000
```

## Handler files

Name handler files after the state or action they serve, without verb
prefixes or object nouns where context makes them redundant. Use
kebab-case.

```
// avoid
get-my-squad.js
place-a-bid.js

// prefer
squad.js
bid.js
```

## Test files

Name test files after the handler/module file they test, with a
`.test.js`/`.test.ts` suffix.

```
// avoid
get-my-squad.test.js

// prefer
squad.test.js
```

## Handler factories

Name handler factories with a `create` prefix, a PascalCase description of
what the handler does, and a `Handler` suffix (see
[handler-factory-pattern.md](./handler-factory-pattern.md)).

```js
// avoid
const squadHandler = (deps) => { ... }
const handleSquad = (deps) => { ... }

// prefer
const createSquadHandler = ({ biwengerClient }) => { ... }
```

**Why:** kebab-case files are predictable on case-insensitive filesystems
and consistent with Node.js ecosystem conventions. camelCase functions
match JavaScript/TypeScript idiom. The `create` prefix signals a factory
that returns a handler, not the handler itself. The `Handler` suffix
distinguishes the factory from domain or infrastructure factories.

*(Ported from interest-tracker's coding conventions, adapted to this
project. Applies once there's more than one handler — a single Slice-1
handler doesn't yet need every rule enforced literally.)*
