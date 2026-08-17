# ADR-001: Use interest-tracker-android's Event-Driven Architecture

**Status**: Accepted

---

## Context

This app needed an architecture from its first screen. interest-tracker-android
already has a working, tested event-driven architecture (MVI-like:
Event → Effect → State via a `Registry`/`Database`) proven across four
features. Nothing about this app's single squad screen calls for a
different pattern.

## Decision

Port the architecture wholesale: `Registry`/`ChannelRegistry`, `Store`/
`AppStore`, `Database`, `StateInitializer`, `Coeffect`/`Coeffects`/
`CoeffectHandler`, `Effect`/`EffectHandler`, `Event`. One feature package
(`features/squad/`) follows the same internal shape
(`domain/`/`infrastructure/`/`ui/`) as interest-tracker-android's
features.

Not ported: `NavigationEffect`/`Navigator`/`Routes` (one screen, nothing
to navigate to) and the response-caching layer (`ResponseCache`/
`CachingXxxService`) — one API call, not yet a caching concern. Both are
straightforward to add the slice a second screen or a caching need
actually appears.

## Consequences

- Positive: proven, already-documented architecture; no design-from-scratch risk.
- Positive: any future work on this app can lean on interest-tracker-android's
  ADRs/coding-conventions almost directly.
- Negative: for a single-screen app, the machinery (Registry, Database,
  Coeffects) is more indirection than a minimal MVVM screen would need.
  Accepted because this app is expected to grow past one screen, and
  retrofitting the architecture later costs more than carrying it now.
