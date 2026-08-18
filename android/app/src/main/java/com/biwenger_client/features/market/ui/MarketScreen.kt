package com.biwenger_client.features.market.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.ui.MatchDayDetailsScreen
import com.biwenger_client.ui.PlayerAvatarOverlayOffsetY
import com.biwenger_client.ui.PlayerAvatarWithPoints
import com.biwenger_client.ui.PlayerDetailScreen
import com.biwenger_client.ui.PositionTag
import com.biwenger_client.ui.formatPrice
import com.biwenger_client.ui.formatPriceChange
import com.biwenger_client.ui.priceTrend
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral500
import com.biwenger_client.ui.theme.NocturneRadius
import java.util.Calendar
import kotlin.math.ceil

// Slice 1 shipped list-only (name/position/price); this fills in the
// three fields deferred then — expiry, seller, and market value — since
// a bare asking price without knowing what the player's actually worth
// or how long the listing lasts isn't enough to act on. Tapping a row
// opens the same shared player-detail sheet Squad uses (price/performance
// history, match-day drill-down) — no bidding yet, that's still out of
// scope, see docs/backlog.
@Composable
fun MarketScreen(
    viewModel: MarketViewModel = hiltViewModel()
) {
    val players by viewModel.players
    val selectedPlayerId by viewModel.selectedPlayerId
    val priceHistory by viewModel.priceHistory
    val performanceHistory by viewModel.performanceHistory
    val performanceHistorySeason by viewModel.performanceHistorySeason
    val selectedMatchDay by viewModel.selectedMatchDay
    val matchDayDetails by viewModel.matchDayDetails

    MarketScreen(
        players = players,
        selectedPlayerId = selectedPlayerId,
        priceHistory = priceHistory,
        performanceHistory = performanceHistory,
        performanceHistorySeason = performanceHistorySeason,
        selectedMatchDay = selectedMatchDay,
        matchDayDetails = matchDayDetails,
        onPlayerTapped = viewModel::playerTapped,
        onPerformanceSeasonChanged = viewModel::performanceSeasonChanged,
        onSheetDismissed = viewModel::sheetClosed,
        onMatchDayTapped = viewModel::matchDayTapped,
        onMatchDayDetailsDismissed = viewModel::matchDayDetailsClosed,
    )
}

@Composable
private fun MarketScreen(
    players: Loadable<List<MarketListing>>,
    selectedPlayerId: Int?,
    priceHistory: Loadable<PriceHistory>?,
    performanceHistory: Loadable<PerformanceHistory>?,
    performanceHistorySeason: String,
    selectedMatchDay: Int?,
    matchDayDetails: Loadable<MatchDayDetails>?,
    onPlayerTapped: (Int) -> Unit,
    onPerformanceSeasonChanged: (Int, String) -> Unit,
    onSheetDismissed: () -> Unit,
    onMatchDayTapped: (Int, Int, String) -> Unit,
    onMatchDayDetailsDismissed: () -> Unit,
) {
    val allListings = (players as? Loadable.Success)?.value.orEmpty()
    val selectedListing = allListings.find { it.id == selectedPlayerId }

    // Exclusive, not overlaid — same reasoning as SquadScreen: only one
    // screen is ever composed at a time.
    if (selectedListing != null && selectedMatchDay != null) {
        MatchDayDetailsScreen(player = selectedListing.toPlayer(), matchDayDetails = matchDayDetails, onBack = onMatchDayDetailsDismissed)
    } else if (selectedListing != null) {
        PlayerDetailScreen(
            player = selectedListing.toPlayer(),
            priceHistory = priceHistory,
            performanceHistory = performanceHistory,
            performanceHistorySeason = performanceHistorySeason,
            onPerformanceSeasonChanged = { season -> onPerformanceSeasonChanged(selectedListing.id, season) },
            onMatchDayTapped = { matchDay -> onMatchDayTapped(selectedListing.id, matchDay, performanceHistorySeason) },
            onBack = onSheetDismissed
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            MarketHeader()

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (players) {
                    is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is Loadable.Failed -> Text(
                        text = "Could not load the market right now.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    is Loadable.Success -> MarketListingList(listings = players.value, onPlayerTapped = onPlayerTapped)
                }
            }
        }
    }
}

@Composable
private fun MarketHeader() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = "Market", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun MarketListingList(listings: List<MarketListing>, onPlayerTapped: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listings) { listing -> MarketListingRow(listing = listing, onClick = { onPlayerTapped(listing.id) }) }
    }
}

@Composable
private fun MarketListingRow(listing: MarketListing, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Listing metadata (who's selling, how long it's live) — not
        // about the player itself, hence its own row above the content.
        MarketListingHeader(listing = listing)
        // The player being sold and the asking price against its trend —
        // the card's headline fact.
        MarketListingContent(listing = listing)
        // Supporting context (catalogue value + its own increment) that
        // qualifies the content above rather than being the headline.
        MarketListingFooter(listing = listing)
    }
}

@Composable
private fun MarketListingHeader(listing: MarketListing) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // maxLines/overflow as a guard against a long seller name
        // overflowing the row — not observed to actually wrap in
        // practice; the perceived top-row misalignment this was
        // chasing turned out not to be one (same fontSize/color on
        // both, neither wraps — likely just the usual left-vs-right
        // text optical effect). CenterVertically kept anyway, it's a
        // correct choice regardless.
        Text(
            text = sellerLabel(listing.seller),
            fontSize = 13.sp,
            color = Neutral500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            // The time component ("tomorrow", "2 days", "2 hours"...) is
            // the urgent part of this label — bolded so it reads at a
            // glance, "Expires" stays regular weight as the lead-in.
            text = buildAnnotatedString {
                append("Expires ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(formatExpiry(listing.until))
                }
            },
            fontSize = 13.sp,
            color = Neutral500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun MarketListingContent(listing: MarketListing) {
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        PlayerAvatarWithPoints(
            photoUrl = listing.photoUrl,
            teamCrestUrl = listing.teamCrestUrl,
            contentDescription = listing.name,
            points = listing.points
        )

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = listing.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(modifier = Modifier.padding(top = 4.dp)) {
                PositionTag(position = listing.position, secondaryPosition = listing.secondaryPosition)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatPrice(listing.price), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
            // How the asking price compares to market value — same
            // up/down/flat styling as the market value increment
            // below, just a different underlying number (price -
            // marketValue, not the catalogue's own day-over-day move).
            val (icon, color) = priceTrend(listing.price - listing.marketValue)
            Text(
                text = "$icon ${formatPriceChange(listing.price - listing.marketValue)}",
                fontSize = 12.sp,
                color = color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun MarketListingFooter(listing: MarketListing) {
    // The increment tracks the catalogue's live value, not the fixed
    // asking price above — it goes right after market value, not
    // under the price. Top padding is content's 8dp plus the points
    // badge/crest overhang (PlayerAvatarOverlayOffsetY) — that overlay
    // pokes past the content row's layout bounds without adding to its
    // measured height, so matching header-to-content's 8dp exactly here
    // would look tighter than it is.
    Row(
        modifier = Modifier.padding(top = 8.dp + PlayerAvatarOverlayOffsetY),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Market value: ${formatPrice(listing.marketValue)} ", fontSize = 13.sp, color = Neutral500)
        val (icon, color) = priceTrend(listing.priceIncrement)
        Text(text = "$icon ${formatPriceChange(listing.priceIncrement)}", fontSize = 13.sp, color = color)
    }
}

private fun sellerLabel(seller: String?): String = seller ?: "Free agent"

// Relative, per how urgent a listing is:
// - <8h away, or expiring today: "in N hours"
// - expiring tomorrow: "tomorrow"
// - otherwise: "in N days"
fun formatExpiry(until: Long, now: Long = System.currentTimeMillis()): String {
    val untilMillis = until * 1000
    val diffHours = (untilMillis - now) / (1000.0 * 60 * 60)
    val dayDiff = calendarDayDiff(untilMillis = untilMillis, nowMillis = now)

    return when {
        dayDiff <= 0 || diffHours < 8 -> "in ${ceil(diffHours).toLong().coerceAtLeast(1)} hours"
        dayDiff == 1L -> "tomorrow"
        else -> "in $dayDiff days"
    }
}

private fun calendarDayDiff(untilMillis: Long, nowMillis: Long): Long {
    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    val millisPerDay = 1000L * 60 * 60 * 24
    return (startOfDay(untilMillis) - startOfDay(nowMillis)) / millisPerDay
}
