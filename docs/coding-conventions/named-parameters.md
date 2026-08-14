# Always Use Named Parameters in Kotlin Function Calls

All function and constructor calls use named parameters, including
single-parameter calls and trailing lambdas.

```kotlin
// Wrong
SquadScreen(players)

// Correct
SquadScreen(players = players)
```

**Why**: positional arguments require the reader to mentally match
position to parameter name. Named parameters make intent explicit and
make reordering safe.
