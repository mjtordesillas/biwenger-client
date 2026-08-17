# Project Structure

```
com.biwenger_client/
├── core/           # MVI framework — events, effects, coeffects, state, registry
├── features/       # One subpackage per feature
├── infrastructure/ # Cross-feature HTTP client
├── shared/         # AppModule, EffectsHandlerRegistration, CoeffectsHandlerRegistration
└── ui/             # Global Compose theme
```

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
