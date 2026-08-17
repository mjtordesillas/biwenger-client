package com.biwenger_client.features.market.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.ui.PlayerAvatar
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
// or how long the listing lasts isn't enough to act on. No tap
// interaction/bidding yet — still out of scope, see docs/backlog.
@Composable
fun MarketScreen(
    viewModel: MarketViewModel = hiltViewModel()
) {
    val players by viewModel.players
    MarketScreen(players = players)
}

@Composable
private fun MarketScreen(players: Loadable<List<MarketListing>>) {
    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (players) {
                is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Loadable.Failed -> Text(
                    text = "Could not load the market right now.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is Loadable.Success -> MarketListingList(listings = players.value)
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
private fun MarketListingList(listings: List<MarketListing>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listings) { listing -> MarketListingRow(listing = listing) }
    }
}

@Composable
private fun MarketListingRow(listing: MarketListing) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = sellerLabel(listing.seller), fontSize = 11.sp, color = Neutral500)
            Text(text = "Expires ${formatExpiry(listing.until)}", fontSize = 11.sp, color = Neutral500)
        }

        Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatar(
                photoUrl = listing.photoUrl,
                teamCrestUrl = listing.teamCrestUrl,
                contentDescription = listing.name,
                size = 48.dp
            )

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(text = listing.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    PositionTag(position = listing.position, secondaryPosition = listing.secondaryPosition)
                }
            }

            Text(text = formatPrice(listing.price), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
        }

        // The increment tracks the catalogue's live value, not the fixed
        // asking price above — it goes right after market value, not
        // under the price.
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Market value: ${formatPrice(listing.marketValue)} ", fontSize = 11.sp, color = Neutral500)
            val (icon, color) = priceTrend(listing.priceIncrement)
            Text(text = "$icon ${formatPriceChange(listing.priceIncrement)}", fontSize = 11.sp, color = color)
        }
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
