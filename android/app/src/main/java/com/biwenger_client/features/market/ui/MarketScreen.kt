package com.biwenger_client.features.market.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
import com.biwenger_client.features.market.domain.models.PlayerOffer
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
import com.biwenger_client.ui.formatRelativeTime
import com.biwenger_client.ui.priceTrend
import com.biwenger_client.ui.theme.ColorBgDeep
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral500
import com.biwenger_client.ui.theme.NocturneRadius

// Local to this screen, same as SquadSubTab — nothing outside Market
// depends on which subtab is showing.
private enum class MarketSubTab { CurrentMarket, MyListings, Offers, Bids }

// Expiry ascending first (soonest-to-expire listings are the most
// actionable, so they lead), then position and market value both
// descending.
val MarketListingOrder =
    compareBy<MarketListing> { it.until }
        .thenByDescending { it.position }
        .thenByDescending { it.marketValue }

// Same reasoning as MarketListingOrder: expiry ascending first (soonest
// to lapse is most actionable), then position and offer amount both
// descending.
val PlayerOfferOrder =
    compareBy<PlayerOffer> { it.until }
        .thenByDescending { it.position }
        .thenByDescending { it.amount }

// Same reasoning again, for my own outgoing bids.
val PlayerBidOrder =
    compareBy<PlayerBid> { it.until }
        .thenByDescending { it.position }
        .thenByDescending { it.amount }

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
    val myListings by viewModel.myListings
    val offers by viewModel.offers
    val bids by viewModel.bids
    val selectedPlayerId by viewModel.selectedPlayerId
    val priceHistory by viewModel.priceHistory
    val performanceHistory by viewModel.performanceHistory
    val performanceHistorySeason by viewModel.performanceHistorySeason
    val selectedMatchDay by viewModel.selectedMatchDay
    val matchDayDetails by viewModel.matchDayDetails

    MarketScreen(
        players = players,
        myListings = myListings,
        offers = offers,
        bids = bids,
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
    myListings: Loadable<List<MarketListing>>,
    offers: Loadable<List<PlayerOffer>>,
    bids: Loadable<List<PlayerBid>>,
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
    val allMyListings = (myListings as? Loadable.Success)?.value.orEmpty()
    val allOffers = (offers as? Loadable.Success)?.value.orEmpty()
    val allBids = (bids as? Loadable.Success)?.value.orEmpty()
    // A tapped row can come from any of the four subtabs' lists — each
    // is a different type, so each is looked up on its own list and
    // reduced to the shared Player shape the detail sheet actually
    // needs, rather than combined into one list first.
    val selectedPlayer: Player? =
        allListings.find { it.id == selectedPlayerId }?.toPlayer()
            ?: allMyListings.find { it.id == selectedPlayerId }?.toPlayer()
            ?: allOffers.find { it.id == selectedPlayerId }?.toPlayer()
            ?: allBids.find { it.id == selectedPlayerId }?.toPlayer()

    // Exclusive, not overlaid — same reasoning as SquadScreen: only one
    // screen is ever composed at a time.
    if (selectedPlayer != null && selectedMatchDay != null) {
        MatchDayDetailsScreen(player = selectedPlayer, matchDayDetails = matchDayDetails, onBack = onMatchDayDetailsDismissed)
    } else if (selectedPlayer != null) {
        PlayerDetailScreen(
            player = selectedPlayer,
            priceHistory = priceHistory,
            performanceHistory = performanceHistory,
            performanceHistorySeason = performanceHistorySeason,
            onPerformanceSeasonChanged = { season -> onPerformanceSeasonChanged(selectedPlayer.id, season) },
            onMatchDayTapped = { matchDay -> onMatchDayTapped(selectedPlayer.id, matchDay, performanceHistorySeason) },
            onBack = onSheetDismissed
        )
    } else {
        // Local to this composable, not routed through the Registry —
        // same reasoning as SquadScreen's selectedSubTab.
        var selectedSubTab by remember { mutableStateOf(MarketSubTab.CurrentMarket) }

        Column(modifier = Modifier.fillMaxSize()) {
            MarketSubTabRow(selected = selectedSubTab, onSelect = { selectedSubTab = it })

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
                when (selectedSubTab) {
                    MarketSubTab.CurrentMarket -> MarketListingListForState(
                        listings = players,
                        emptyMessage = "Could not load the market right now.",
                        onPlayerTapped = onPlayerTapped
                    )
                    MarketSubTab.MyListings -> MarketListingListForState(
                        listings = myListings,
                        emptyMessage = "Could not load your listings right now.",
                        onPlayerTapped = onPlayerTapped
                    )
                    MarketSubTab.Offers -> PlayerOfferListForState(
                        offers = offers,
                        emptyMessage = "Could not load your offers right now.",
                        onPlayerTapped = onPlayerTapped
                    )
                    MarketSubTab.Bids -> PlayerBidListForState(
                        bids = bids,
                        emptyMessage = "Could not load your bids right now.",
                        onPlayerTapped = onPlayerTapped
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.MarketListingListForState(
    listings: Loadable<List<MarketListing>>,
    emptyMessage: String,
    onPlayerTapped: (Int) -> Unit,
) {
    when (listings) {
        is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is Loadable.Failed -> Text(
            text = emptyMessage,
            modifier = Modifier.align(Alignment.Center).padding(16.dp)
        )
        is Loadable.Success -> MarketListingList(
            listings = listings.value.sortedWith(MarketListingOrder),
            onPlayerTapped = onPlayerTapped
        )
    }
}

@Composable
private fun BoxScope.PlayerOfferListForState(
    offers: Loadable<List<PlayerOffer>>,
    emptyMessage: String,
    onPlayerTapped: (Int) -> Unit,
) {
    when (offers) {
        is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is Loadable.Failed -> Text(
            text = emptyMessage,
            modifier = Modifier.align(Alignment.Center).padding(16.dp)
        )
        is Loadable.Success -> PlayerOfferList(
            offers = offers.value.sortedWith(PlayerOfferOrder),
            onPlayerTapped = onPlayerTapped
        )
    }
}

@Composable
private fun BoxScope.PlayerBidListForState(
    bids: Loadable<List<PlayerBid>>,
    emptyMessage: String,
    onPlayerTapped: (Int) -> Unit,
) {
    when (bids) {
        is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is Loadable.Failed -> Text(
            text = emptyMessage,
            modifier = Modifier.align(Alignment.Center).padding(16.dp)
        )
        is Loadable.Success -> PlayerBidList(
            bids = bids.value.sortedWith(PlayerBidOrder),
            onPlayerTapped = onPlayerTapped
        )
    }
}

// Same full-width, equal-split, underlined tab bar as Squad's
// SquadSubTabRow — ported into Market rather than extracted into a
// shared component, per docs/backlog/to-do/view-my-market-listings.md.
@Composable
private fun MarketSubTabRow(selected: MarketSubTab, onSelect: (MarketSubTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(ColorBgDeep)) {
        MarketSubTabButton(
            label = "Market",
            icon = { color -> Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) },
            selected = selected == MarketSubTab.CurrentMarket,
            onClick = { onSelect(MarketSubTab.CurrentMarket) },
            modifier = Modifier.weight(1f)
        )
        MarketSubTabButton(
            label = "My Listings",
            icon = { color -> Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) },
            selected = selected == MarketSubTab.MyListings,
            onClick = { onSelect(MarketSubTab.MyListings) },
            modifier = Modifier.weight(1f)
        )
        MarketSubTabButton(
            label = "Offers",
            icon = { color -> Icon(imageVector = Icons.Outlined.Payments, contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) },
            selected = selected == MarketSubTab.Offers,
            onClick = { onSelect(MarketSubTab.Offers) },
            modifier = Modifier.weight(1f)
        )
        MarketSubTabButton(
            label = "Bids",
            icon = { color -> Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) },
            selected = selected == MarketSubTab.Bids,
            onClick = { onSelect(MarketSubTab.Bids) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarketSubTabButton(
    label: String,
    icon: @Composable (Color) -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else Neutral500
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                drawLine(
                    color = if (selected) color else Color.Transparent,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(vertical = 10.dp)
    ) {
        // Stacked, not side by side — four tabs' worth of icon+label
        // side by side got too tight for "Current Market"/"My Listings"
        // at equal 1/4 width; stacking gives each label its own full
        // width line to wrap/ellipsis into instead of fighting the icon
        // for horizontal space.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            icon(color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
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
                    append(formatRelativeTime(listing.until))
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

@Composable
private fun PlayerOfferList(offers: List<PlayerOffer>, onPlayerTapped: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(offers) { offer -> PlayerOfferRow(offer = offer, onClick = { onPlayerTapped(offer.id) }) }
    }
}

// Same header/content/footer shape as MarketListingRow, with the fields
// that differ for an offer: no expiry (an offer doesn't have one, unlike
// a listing), amount instead of an asking price.
@Composable
private fun PlayerOfferRow(offer: PlayerOffer, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        PlayerOfferHeader(offer = offer)
        PlayerOfferContent(offer = offer)
        PlayerOfferFooter(offer = offer)
    }
}

@Composable
private fun PlayerOfferHeader(offer: PlayerOffer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "From: ${bidderLabel(offer.bidder)}",
            fontSize = 13.sp,
            color = Neutral500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            // Same "Expires <relative time>" shape as MarketListingHeader.
            text = buildAnnotatedString {
                append("Expires ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(formatRelativeTime(offer.until))
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
private fun PlayerOfferContent(offer: PlayerOffer) {
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        PlayerAvatarWithPoints(
            photoUrl = offer.photoUrl,
            teamCrestUrl = offer.teamCrestUrl,
            contentDescription = offer.name,
            points = offer.points
        )

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = offer.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(modifier = Modifier.padding(top = 4.dp)) {
                PositionTag(position = offer.position, secondaryPosition = offer.secondaryPosition)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatPrice(offer.amount), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
            // How the offer compares to market value — same reasoning
            // Squad's existing offer-amount badge uses (>= counts as
            // "above value").
            val (icon, color) = priceTrend(offer.amount - offer.price)
            Text(
                text = "$icon ${formatPriceChange(offer.amount - offer.price)}",
                fontSize = 12.sp,
                color = color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PlayerOfferFooter(offer: PlayerOffer) {
    Row(
        modifier = Modifier.padding(top = 8.dp + PlayerAvatarOverlayOffsetY),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Market value: ${formatPrice(offer.price)} ", fontSize = 13.sp, color = Neutral500)
        val (icon, color) = priceTrend(offer.priceIncrement)
        Text(text = "$icon ${formatPriceChange(offer.priceIncrement)}", fontSize = 13.sp, color = color)
    }
}

private fun bidderLabel(bidder: String?): String = bidder ?: "the Market"

@Composable
private fun PlayerBidList(bids: List<PlayerBid>, onPlayerTapped: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bids) { bid -> PlayerBidRow(bid = bid, onClick = { onPlayerTapped(bid.id) }) }
    }
}

// Same header/footer shape as MarketListingRow (owner/expiry header,
// market-value footer) — a bid's player has both, same as a listing's.
// The content differs: three numbers instead of one, since a bid has an
// asking price *and* my own offer against it, not just one price.
@Composable
private fun PlayerBidRow(bid: PlayerBid, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        PlayerBidHeader(bid = bid)
        PlayerBidContent(bid = bid)
        PlayerBidFooter(bid = bid)
    }
}

@Composable
private fun PlayerBidHeader(bid: PlayerBid) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sellerLabel(bid.seller),
            fontSize = 13.sp,
            color = Neutral500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = buildAnnotatedString {
                append("Expires ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(formatRelativeTime(bid.until))
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
private fun PlayerBidContent(bid: PlayerBid) {
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        PlayerAvatarWithPoints(
            photoUrl = bid.photoUrl,
            teamCrestUrl = bid.teamCrestUrl,
            contentDescription = bid.name,
            points = bid.points
        )

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = bid.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(modifier = Modifier.padding(top = 4.dp)) {
                PositionTag(position = bid.position, secondaryPosition = bid.secondaryPosition)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            // Asking price, de-emphasized — my own offer underneath is
            // the headline number here, not what's being asked.
            Text(
                text = formatPrice(bid.price),
                fontSize = 12.sp,
                color = Neutral500,
            )
            Text(
                text = formatPrice(bid.amount),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
            // My offer against the catalogue market value (not the
            // asking price above it) — whether the bid is above/below
            // fair value, independent of what's being asked.
            val (icon, color) = priceTrend(bid.amount - bid.marketValue)
            Text(
                text = "$icon ${formatPriceChange(bid.amount - bid.marketValue)}",
                fontSize = 12.sp,
                color = color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PlayerBidFooter(bid: PlayerBid) {
    Row(
        modifier = Modifier.padding(top = 8.dp + PlayerAvatarOverlayOffsetY),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Market value: ${formatPrice(bid.marketValue)} ", fontSize = 13.sp, color = Neutral500)
        val (icon, color) = priceTrend(bid.priceIncrement)
        Text(text = "$icon ${formatPriceChange(bid.priceIncrement)}", fontSize = 13.sp, color = color)
    }
}
