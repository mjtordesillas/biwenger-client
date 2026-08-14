# Coeffects — How to Declare, Register, and Consume

## 1. Declare on the ViewModel

```kotlin
private val squadCoeffect = FetchSquadCoeffect
```

## 2. Register with the handler

```kotlin
store.registerEventHandler(
    name = "squad.on-load",
    coeffects = listOf(squadCoeffect),
    handler = ::handleOnLoad
)
```

## 3. Consume in the handler

Every coeffect delivers `Loadable<T>`. Use `coeffects.load(cfx)`. The
handler must not reference `database`, `store`, or any service.

```kotlin
fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
    listOf(UpdateState(path = "squad.players", value = coeffects.load(coeffect = squadCoeffect)))
```

## 4. Failure handling

Handlers throw from `extract` on failure — never return a default value.
The framework catches, calls `onFailure`, delivers `Loadable.Failed`.

## Adding a new coeffect type

1. Create the coeffect and handler in `features/<name>/domain/coeffects/`.
2. Register in `<Feature>CoeffectsHandlerRegistration`.
3. Add the service to that registration's constructor, wire it up in `AppModule`.

## When the coeffect needs data from the event itself

Use a `data class` coeffect (not an `object`) and the event-parameterized
`registerEventHandler` overload — see
`docs/adrs/ADR-009-event-parameterized-coeffects.md`:

```kotlin
data class FetchPriceHistoryCoeffect(val playerId: Int) : Coeffect<PriceHistory>

store.registerEventHandler(
    name = PLAYER_TAPPED_EVENT,
    coeffects = { event -> listOf(FetchPriceHistoryCoeffect(playerId = event.payload)) },
    handler = ::handlePlayerTapped
)

fun handlePlayerTapped(event: Event<Int>, coeffects: Coeffects): List<Effect> =
    listOf(UpdateState(
        path = "squad.priceHistory",
        value = coeffects.load(coeffect = FetchPriceHistoryCoeffect(playerId = event.payload))
    ))
```

Structural equality (`data class`) is what lets `coeffects.load(...)`
find the value resolved from the equivalent instance the selector built.
