package com.biwenger_client.features.squad.ui

import androidx.compose.ui.graphics.Color

// Position and price-trend colors are functional status, not decoration —
// the one exception Nocturne's readme carves out from "keep chroma low
// outside the accent". Feature-local, not promoted to ui/theme/Color.kt,
// since nothing outside the squad screen needs them yet.
val PositionColors = mapOf(
    1 to Color(0xFFE3A83F), // GK
    2 to Color(0xFF4C8FD9), // DF
    3 to Color(0xFF45B57C), // MF
    4 to Color(0xFFDF5B52), // FW
)

val TrendUp = Color(0xFF3ECF72)
val TrendDown = Color(0xFFE0554F)
val TrendFlat = Color(0xFF9397AB) // ui.theme.Neutral500
