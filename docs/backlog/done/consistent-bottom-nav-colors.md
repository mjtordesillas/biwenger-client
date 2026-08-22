The bottom navigation bar (Squad/Market) should follow the same color
scheme as the subtab rows at the top of each screen (Squad's
Players/Lineup, Market's four subtabs) — it didn't.

`BiwengerClientNavigationBar` was still on Material3's stock
`NavigationBar` colors: default `containerColor`
(`MaterialTheme.colorScheme.surfaceContainer`) and `ColorAccent` for
the selected item only (no explicit unselected color, so Material's
own `onSurfaceVariant` default). The top subtab rows
(`SquadSubTabRow`/`MarketSubTabRow`) use `ColorBgDeep` for the bar
background and switch each item's icon/label color between
`MaterialTheme.colorScheme.primary` (selected) and `Neutral500`
(unselected) — no pill/indicator behind the icon, just the color swap.
`ColorBgDeep`'s own doc comment already called this out ("a shade below
ColorBg, for a nav bar that should read as a distinct layer from page
content") but was never actually wired up to the nav bar it was named
for. `BiwengerClientNavigationBar` now matches exactly: `containerColor
= ColorBgDeep`, `selectedIconColor`/`selectedTextColor =
MaterialTheme.colorScheme.primary`, `unselectedIconColor`/
`unselectedTextColor = Neutral500`, indicator still transparent.
