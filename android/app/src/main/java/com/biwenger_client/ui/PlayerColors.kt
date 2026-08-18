package com.biwenger_client.ui

import androidx.compose.ui.graphics.Color

// Position and price-trend colors are functional status, not decoration —
// the one exception Nocturne's readme carves out from "keep chroma low
// outside the accent". Promoted out of features/squad — market's player
// rows need the same PositionColors/Trend* now too (PerformanceLow/Mid/
// High/Great stay here too rather than splitting the file; unused by
// market for now, harmless).
val PositionColors = mapOf(
    1 to Color(0xFFE3A83F), // GK
    2 to Color(0xFF4C8FD9), // DF
    3 to Color(0xFF45B57C), // MF
    4 to Color(0xFFDF5B52), // FW
)

val TrendUp = Color(0xFF3ECF72)
val TrendDown = Color(0xFFE0554F)
val TrendFlat = Color(0xFF9397AB) // ui.theme.Neutral500

val PerformanceLow = Color(0xFFE0554F) // < 2 pts
val PerformanceMid = Color(0xFFE3C23F) // 2–4 pts
val PerformanceHigh = Color(0xFF3ECF72) // 5–9 pts
val PerformanceGreat = Color(0xFF4C8FD9) // 10+ pts

// Squad player fitness status — same semantic-color exception, aliased
// from the closest existing functional color rather than new hex values.
val StatusInjured = TrendDown
val StatusDoubt = PerformanceMid
