# Nocturne design system (Android/Compose port)

Nocturne is a quiet, compact dark interface: a near-neutral blue-grey ground, Inter at medium weight, soft 8px radii and an accent used as a line and a glow rather than a flood. Contrast comes from the tonal ramps, not from saturation, and photographs blend into the page with their dark values falling away.

This is an adaptation of Nocturne's original web design system (CSS/HTML) for this native Compose app. It keeps the source's design language and voice, but replaces anything that assumed a browser. Where this file's guidance conflicts with the original, this file is the source of truth for this repo.

## How to use this

- Every color, spacing, radius, weight and font size comes from `app/src/main/java/com/biwenger_client/ui/theme/` — `Color.kt`, `Theme.kt` (`NocturneRadius`, `NocturneSpace`), `Type.kt`. Never hard-code a hex, a font name, or a dp/sp value the theme already carries.
- There is no shared component library file (no Compose equivalent of the original's `.card`/`.btn`/`.tag` classes) — each feature builds the composables it needs, following the same visual rules, in its own `ui/` package (e.g. `features/squad/ui/`). Promote something to a shared location only once a second feature needs the same composable.
- `docs/adrs/` records the decisions made while adapting this system to Compose.

## Direction

Left-aligned, asymmetric layouts. Buttons are outlined (1px accent border on transparent), not solid-filled. Grounds stay desaturated, with soft depth rather than flat fills. Prefer photographs shot on dark or black backgrounds.

## Color

A dark ground (`--color-bg` #161826) with `--color-text` #e9e9ed and a single accent #9184d9 — a blurple, at the chroma that hue carries in the app, so the accent reads as an accent against the desaturated ramps. Each role carries a 100–900 tonal ramp generated in OKLCH on a shared perceptual lightness scale. On this dark ground use the dark steps (700–900) for tinted fills, hovers and subtle borders, 500 as the role's base, and the light steps (100–300) for text on those tints. For elevation use the shadow tokens rather than ad-hoc shadows.

Position and price-trend colors (GK/DF/MF/FW, up/down/flat) are the one deliberate exception to "keep chroma low outside the accent" — functional status, not decoration. They're feature-local (`features/squad/ui/PlayerColors.kt`), not promoted to the shared theme, since nothing outside the squad screen needs them yet.

## Type

Inter for headings over Inter for body text. Density 0.70× and radius 8px are baked into the spacing/radius scale. Headings never bolden past weight 500 — hierarchy here is size and space, not boldness.

Bundled as real font files (`res/font/inter_400.ttf`/`500`/`600`/`700`), not fetched via a downloadable-font provider — see `docs/adrs/`.

## Icons

The original system specifies Phosphor icons throughout. No Phosphor Compose library exists (checked Maven Central — no results), so this app uses `androidx.compose.material:material-icons-extended` instead, as the closest reliably-available equivalent. Use it for any icon need, app-wide.

## Interaction states

Interactive elements get a themed pressed/hover state and a visible focus indicator — never bare platform defaults. Compose's default ripple is acceptable (Nocturne's readme doesn't prohibit it; it just insists states are never left unstyled).

## Do

- Keep chroma low outside the accent and the position/trend status colors — lean on the neutral ramp for surfaces, borders and muted text.
- Use the compact spacing scale (density 0.7×) — this system is dense on purpose.
- Outline primary actions rather than filling them.

## Don't

- Do not flood large areas with the accent or any saturated fill.
- Do not use pure black or pure white — every value comes from the ramps.
- Do not stack heavy shadows.
- Do not bolden headings past weight 500.

## Files

- `app/src/main/java/com/biwenger_client/ui/theme/Color.kt` — every color token
- `app/src/main/java/com/biwenger_client/ui/theme/Theme.kt` — `NocturneRadius`, `NocturneSpace`, the Compose `ColorScheme`
- `app/src/main/java/com/biwenger_client/ui/theme/Type.kt` — Inter `FontFamily` and `Typography`
- `app/src/main/res/font/` — the bundled Inter font files
- `app/src/main/java/com/biwenger_client/features/squad/ui/` — the squad screen's composables and its feature-local status colors (`PlayerColors.kt`)
- `docs/adrs/` — decisions made while adapting this system to Compose
