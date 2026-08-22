package com.biwenger_client.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Same PullToRefreshBox every screen uses (Squad, Lineup, Market's four
// subtabs — see docs/backlog/done/refresh-screen.md). Doesn't use
// PullToRefreshDefaults.Indicator (the stock arrow-then-spinner one) —
// two problems with it, worked around here:
//
// - No way to drop its elevation (PullToRefreshDefaults.Indicator
//   doesn't expose that param, only the lower-level
//   Modifier.pullToRefreshIndicator does) — its Material shadow
//   rendered as a visible hexagon artifact on-device, and a flat circle
//   matches this app's own flat-pill button style anyway (nothing else
//   on screen casts a shadow).
// - It keeps showing (crossfaded into a spinner) for the whole
//   `isRefreshing` duration, on top of the screen's own full-screen
//   spinner (Loadable.Loading, see refresh-screen.md) — two spinners
//   at once.
//
// So this builds a minimal indicator directly off
// Modifier.pullToRefreshIndicator (elevation forced to 0), a plain
// determinate ring keyed to state.distanceFraction instead of the
// stock arrow glyph, shown only while the user is actively dragging —
// once `isRefreshing` flips true it's hidden entirely and the screen's
// own spinner takes over, so the two never overlap.
//
// @OptIn lives here once so callers don't each need their own.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = modifier,
        indicator = {
            if (!isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .pullToRefreshIndicator(
                            state = state,
                            isRefreshing = false,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                            elevation = 0.dp,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = { state.distanceFraction.coerceIn(0f, 1f) },
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        content = content,
    )
}
