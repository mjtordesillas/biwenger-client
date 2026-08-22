package com.biwenger_client.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Same PullToRefreshBox every screen uses (Squad, Lineup, Market's four
// subtabs — see docs/backlog/done/refresh-screen.md), just with the
// pull indicator recolored to the app's own purple accent instead of
// Material3's stock surfaceContainerHigh/onSurfaceVariant defaults,
// which read as a mismatched "default Android" loader against this
// app's dark theme. Same translucent-background/full-opacity-glyph
// schema every other pill/button on screen already uses (see
// MarketListingRow's unlist button, PlayerOfferConfirmationDialog's
// buttons, etc.) — @OptIn lives here once so callers don't each need
// their own.
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
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                color = MaterialTheme.colorScheme.primary,
            )
        },
        content = content,
    )
}
