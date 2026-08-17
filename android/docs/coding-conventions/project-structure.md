# Project Structure

```
com.biwenger_client/
├── core/           # MVI framework — events, effects, coeffects, state, registry, navigation
├── domain/         # Domain models used by more than one feature (e.g. Player)
├── features/       # One subpackage per feature
├── infrastructure/ # Cross-feature HTTP client
├── shared/         # AppModule, EffectsHandlerRegistration, CoeffectsHandlerRegistration
└── ui/             # Global Compose theme, plus components shared by more than one feature (e.g. PlayerList)
```

`domain/` and the shared parts of `ui/` only exist because market became
this app's second real feature — a model or composable starts inside its
originating `features/<name>/` and only moves out once a second feature
actually needs it too (see `AGENT.md`'s "no abstractions before a second
use case").

Each feature follows this internal layout:

```
features/<name>/
├── XxxCoeffectsHandlerRegistration.kt  # Registers coeffect handlers for this feature
├── XxxStateInitializer.kt              # Declares the feature's initial state slice
├── domain/         # Models, effect data classes + handlers, coeffects
├── infrastructure/ # Service interface and implementations
└── ui/             # Composables and ViewModels
```

**Rules**:
- `core/` is framework-only. No feature models, no initial state for a specific feature.
- Feature-specific effects stay in `features/<name>/domain/` — never in `shared/` or `core/`.
- `shared/` contains only startup wiring. No feature logic, no utilities.
