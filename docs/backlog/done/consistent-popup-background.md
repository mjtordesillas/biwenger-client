All popups (dialogs) should use the app's background color. Currently
inconsistent — not all of them do.

Two of the app's four `AlertDialog`s already set `containerColor =
ColorBg` (`PlaceBidDialog`, `ListPlayerPopup` — the latter predates
this backlog item, from `place-a-bid`'s own popup-background feedback).
The other two didn't: `PlayerOfferConfirmationDialog` (reject/accept an
offer, `MarketScreen.kt`) and `SlotOptionsDialog` (fill/replace a
lineup slot, `LineupScreen.kt`) were both still on Material3's default
`AlertDialogDefaults.containerColor` (a light `surfaceContainerHigh`,
clashing with the rest of the app's dark theme). Both now set
`containerColor = ColorBg` too, matching the other two exactly.
