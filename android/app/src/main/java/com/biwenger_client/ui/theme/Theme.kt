package com.biwenger_client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Nocturne is a dark-only system (readme.md: "a quiet, compact dark
// interface") — no light color scheme, unlike Theme.kt's earlier
// isSystemInDarkTheme() branch.
private val NocturneColorScheme = darkColorScheme(
    background = ColorBg,
    surface = ColorSurface,
    onBackground = ColorText,
    onSurface = ColorText,
    onSurfaceVariant = Neutral500,
    primary = ColorAccent,
    onPrimary = ColorText,
    secondary = ColorAccent2,
    outline = ColorDivider,
)

// --radius-sm/md/lg, ported 1:1.
object NocturneRadius {
    val sm = 4.dp
    val md = 8.dp
    val lg = 14.dp
}

// --space-1…8 (density 0.70× already baked in, per readme.md).
object NocturneSpace {
    val s1 = 2.8.dp
    val s2 = 5.6.dp
    val s3 = 8.4.dp
    val s4 = 11.2.dp
    val s6 = 16.8.dp
    val s8 = 22.4.dp
}

@Composable
fun BiwengerClientTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NocturneColorScheme,
        typography = Typography,
        content = content
    )
}
