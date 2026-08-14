# Split Every Screen Composable into Stateful and Stateless Layers

```kotlin
@Composable
fun SquadScreen(
    viewModel: SquadViewModel = hiltViewModel()
) {
    val players by viewModel.players
    SquadScreen(players = players)
}

@Composable
private fun SquadScreen(
    players: Loadable<List<Player>>
) { ... }
```

- Stateless overload is always `private`.
- Callbacks use `() -> Unit` / `(T) -> Unit` — never `KFunction` references.
- All rendering logic, including visibility guards, belongs in the
  stateless overload.
