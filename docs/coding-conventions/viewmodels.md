# ViewModel Conventions

Every screen has a `@HiltViewModel` injecting `Store`. Structure:
subscribe to state paths → register event handlers → dispatch on-load →
deregister in `onCleared`.

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(private val store: Store) : ViewModel() {

    private val myCoeffect = FetchXxxCoeffect

    private val _items = mutableStateOf<Loadable<List<Item>>>(Loadable.Loading)
    val items: State<Loadable<List<Item>>> = _items

    init {
        store.subscribe<Loadable<List<Item>>?>(path = "feature.items") { it?.let { v -> _items.value = v } }
        store.registerEventHandler(name = "feature.on-load", coeffects = listOf(myCoeffect), handler = ::handleOnLoad)
        store.dispatch(event = event(name = "feature.on-load"))
    }

    override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = "feature.on-load", handler = ::handleOnLoad)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "feature.items", value = coeffects.load(coeffect = myCoeffect)))
}
```

## Rules

- Every `registerEventHandler` has a matching `removeEventHandler` in `onCleared`.
- Async-loaded paths: subscribe as `Loadable<T>?` and guard with `?.let`.
- Handlers are named functions (`::handleX`), not lambdas — required for `removeEventHandler` to work.
- Action methods only call `store.dispatch`. No logic.
