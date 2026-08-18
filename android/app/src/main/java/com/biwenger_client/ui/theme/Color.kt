package com.biwenger_client.ui.theme

import androidx.compose.ui.graphics.Color

// Nocturne design system tokens, ported 1:1 from styles.css. Do not
// hand-pick a color outside this file — every value in the UI comes from
// here, matching the source system's "never hard-code a hex" rule.

val ColorBg = Color(0xFF161826)
val ColorBgDeep = Color(0xFF0F111C) // a shade below ColorBg, for a nav bar that should read as a distinct layer from page content
val ColorSurface = Color(0xFF232532)
val ColorText = Color(0xFFE9E9ED)
val ColorAccent = Color(0xFF9184D9)
val ColorAccent2 = Color(0xFFA7A1DB)
val ColorDivider = Color(0x29E9E9ED) // color-mix(text 16%, transparent)

val Neutral100 = Color(0xFFF3F5FE)
val Neutral200 = Color(0xFFE4E7F5)
val Neutral300 = Color(0xFFCFD3E5)
val Neutral400 = Color(0xFFB2B6CA)
val Neutral500 = Color(0xFF9397AB)
val Neutral600 = Color(0xFF75798C)
val Neutral700 = Color(0xFF595D6C)
val Neutral800 = Color(0xFF3F424D)
val Neutral900 = Color(0xFF292B31)

val Accent100 = Color(0xFFF5F4FF)
val Accent200 = Color(0xFFE7E5FE)
val Accent300 = Color(0xFFD2CEFD)
val Accent400 = Color(0xFFB5ABFC)
val Accent500 = Color(0xFF968AE0)
val Accent600 = Color(0xFF796CBF)
val Accent700 = Color(0xFF5D5294)
val Accent800 = Color(0xFF423A6A)
val Accent900 = Color(0xFF2B2741)

// Not in Nocturne's mono palette — semantic status colors for price
// movement, kept feature-local (see PlayerColors.kt) rather than promoted
// to system tokens, matching the readme's "keep chroma low outside the
// accent" rule for everything except functional status.
