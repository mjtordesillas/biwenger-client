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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
import com.biwenger_client.features.market.domain.models.PlayerOffer
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.domain.models.SquadPlayer
import com.biwenger_client.ui.MatchDayDetailsScreen
import com.biwenger_client.ui.PlayerAvatarOverlayOffsetY
import com.biwenger_client.ui.PlayerAvatarWithPoints
import com.biwenger_client.ui.PlayerDetailScreen
import com.biwenger_client.ui.PositionColors
import com.biwenger_client.ui.PositionTag
import com.biwenger_client.ui.TrendDown
import com.biwenger_client.ui.formatPrice
import com.biwenger_client.ui.formatPriceChange
import com.biwenger_client.ui.formatRelativeTime
import com.biwenger_client.ui.priceTrend
import com.biwenger_client.ui.theme.ColorBg
import com.biwenger_client.ui.theme.ColorBgDeep
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.ColorText
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

// The accept-offer action's color — the same green as the MF position
// tag (PositionColors[3]), per the backlog.
private val AcceptGreen = PositionColors.getValue(3)

// The market's own cap on simultaneous listings.
private const val MAX_LISTINGS = 5

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
    val offerToReject by viewModel.offerToReject
    val rejectingOffer by viewModel.rejectingOffer
    val offerToAccept by viewModel.offerToAccept
    val acceptingOffer by viewModel.acceptingOffer
    val unlistingPlayerIds by viewModel.unlistingPlayerIds
    val listPlayerSquad by viewModel.listPlayerSquad
    val listingPlayerIds by viewModel.listingPlayerIds
    val cyclingListings by viewModel.cyclingListings
    val removingBidIds by viewModel.removingBidIds
    val listingToBid by viewModel.listingToBid
    val placingBid by viewModel.placingBid
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
        offerToReject = offerToReject,
        rejectingOffer = rejectingOffer,
        offerToAccept = offerToAccept,
        acceptingOffer = acceptingOffer,
        unlistingPlayerIds = unlistingPlayerIds,
        listPlayerSquad = listPlayerSquad,
        listingPlayerIds = listingPlayerIds,
        cyclingListings = cyclingListings,
        removingBidIds = removingBidIds,
        listingToBid = listingToBid,
        placingBid = placingBid,
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
        onOfferRejectionOpened = viewModel::openOfferRejection,
        onOfferRejectionCancelled = viewModel::cancelOfferRejection,
        onOfferRejected = viewModel::rejectOffer,
        onOfferAcceptanceOpened = viewModel::openOfferAcceptance,
        onOfferAcceptanceCancelled = viewModel::cancelOfferAcceptance,
        onOfferAccepted = viewModel::acceptOffer,
        onUnlistTapped = viewModel::unlistPlayer,
        onListPlayerPopupOpened = viewModel::openListPlayerPopup,
        onListPlayerPopupDismissed = viewModel::closeListPlayerPopup,
        onListTapped = viewModel::listPlayer,
        onCycleTapped = viewModel::cycleListings,
        onRemoveBidTapped = viewModel::removeBid,
        onBidOpened = viewModel::openBid,
        onBidCancelled = viewModel::cancelBid,
        onBidConfirmed = viewModel::placeBid,
    )
}

@Composable
private fun MarketScreen(
    players: Loadable<List<MarketListing>>,
    myListings: Loadable<List<MarketListing>>,
    offers: Loadable<List<PlayerOffer>>,
    bids: Loadable<List<PlayerBid>>,
    offerToReject: PlayerOffer?,
    rejectingOffer: Boolean,
    offerToAccept: PlayerOffer?,
    acceptingOffer: Boolean,
    unlistingPlayerIds: Set<Int>,
    listPlayerSquad: Loadable<List<SquadPlayer>>?,
    listingPlayerIds: Set<Int>,
    cyclingListings: Boolean,
    removingBidIds: Set<Long>,
    listingToBid: MarketListing?,
    placingBid: Boolean,
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
    onOfferRejectionOpened: (PlayerOffer) -> Unit,
    onOfferRejectionCancelled: () -> Unit,
    onOfferRejected: (PlayerOffer) -> Unit,
    onOfferAcceptanceOpened: (PlayerOffer) -> Unit,
    onOfferAcceptanceCancelled: () -> Unit,
    onOfferAccepted: (PlayerOffer) -> Unit,
    onUnlistTapped: (MarketListing) -> Unit,
    onListPlayerPopupOpened: () -> Unit,
    onListPlayerPopupDismissed: () -> Unit,
    onListTapped: (Int) -> Unit,
    onCycleTapped: () -> Unit,
    onRemoveBidTapped: (PlayerBid) -> Unit,
    onBidOpened: (MarketListing) -> Unit,
    onBidCancelled: () -> Unit,
    onBidConfirmed: (MarketListing, Long) -> Unit,
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

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                MarketSubTabRow(selected = selectedSubTab, onSelect = { selectedSubTab = it })

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
                    when (selectedSubTab) {
                        MarketSubTab.CurrentMarket -> MarketListingListForState(
                            listings = players,
                            emptyMessage = "Could not load the market right now.",
                            onPlayerTapped = onPlayerTapped,
                            onBidTapped = onBidOpened,
                        )
                        MarketSubTab.MyListings -> Column(modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                                // Top-left, mirroring "List player"'s
                                // top-right placement — same
                                // tinted-background/full-opacity-text pill
                                // treatment throughout this screen. The
                                // whole batch is now one backend call
                                // (docs/backlog/done/cycle-player-listings.md),
                                // so cyclingListings is a single flag, not
                                // a set of ids to track.
                                Button(
                                    onClick = onCycleTapped,
                                    enabled = !cyclingListings,
                                    shape = RoundedCornerShape(percent = 50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    if (cyclingListings) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    } else {
                                        Text("Cycle listings")
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Button(
                                    onClick = onListPlayerPopupOpened,
                                    enabled = allMyListings.size < MAX_LISTINGS && !cyclingListings,
                                    shape = RoundedCornerShape(percent = 50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                ) {
                                    Text("List player")
                                }
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                MarketListingListForState(
                                    listings = myListings,
                                    emptyMessage = "Could not load your listings right now.",
                                    onPlayerTapped = onPlayerTapped,
                                    onUnlistTapped = onUnlistTapped,
                                    unlistingPlayerIds = unlistingPlayerIds,
                                )
                            }
                        }
                        MarketSubTab.Offers -> PlayerOfferListForState(
                            offers = offers,
                            emptyMessage = "Could not load your offers right now.",
                            onPlayerTapped = onPlayerTapped,
                            onRejectTapped = onOfferRejectionOpened,
                            onAcceptTapped = onOfferAcceptanceOpened,
                        )
                        MarketSubTab.Bids -> PlayerBidListForState(
                            bids = bids,
                            emptyMessage = "Could not load your bids right now.",
                            onPlayerTapped = onPlayerTapped,
                            onRemoveTapped = onRemoveBidTapped,
                            removingBidIds = removingBidIds,
                        )
                    }
                }
            }
            offerToReject?.let { offer ->
                PlayerOfferConfirmationDialog(
                    offer = offer,
                    title = "Reject offer?",
                    actionLabel = "Reject",
                    actionColor = TrendDown,
                    inFlight = rejectingOffer,
                    onCancel = onOfferRejectionCancelled,
                    onConfirm = { onOfferRejected(offer) },
                )
            }
            offerToAccept?.let { offer ->
                PlayerOfferConfirmationDialog(
                    offer = offer,
                    title = "Accept offer?",
                    actionLabel = "Accept",
                    actionColor = AcceptGreen,
                    inFlight = acceptingOffer,
                    onCancel = onOfferAcceptanceCancelled,
                    onConfirm = { onOfferAccepted(offer) },
                )
            }
            listPlayerSquad?.let { squad ->
                ListPlayerPopup(
                    squad = squad,
                    currentListingsCount = allMyListings.size,
                    listingPlayerIds = listingPlayerIds,
                    onDismiss = onListPlayerPopupDismissed,
                    onListTapped = onListTapped,
                )
            }
            listingToBid?.let { listing ->
                PlaceBidDialog(
                    listing = listing,
                    inFlight = placingBid,
                    onCancel = onBidCancelled,
                    onConfirm = { amount -> onBidConfirmed(listing, amount) },
                )
            }
        }
    }
}

@Composable
private fun BoxScope.MarketListingListForState(
    listings: Loadable<List<MarketListing>>,
    emptyMessage: String,
    onPlayerTapped: (Int) -> Unit,
    onUnlistTapped: ((MarketListing) -> Unit)? = null,
    unlistingPlayerIds: Set<Int> = emptySet(),
    onBidTapped: ((MarketListing) -> Unit)? = null,
) {
    when (listings) {
        is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is Loadable.Failed -> Text(
            text = emptyMessage,
            modifier = Modifier.align(Alignment.Center).padding(16.dp)
        )
        is Loadable.Success -> MarketListingList(
            listings = listings.value.sortedWith(MarketListingOrder),
            onPlayerTapped = onPlayerTapped,
            onUnlistTapped = onUnlistTapped,
            unlistingPlayerIds = unlistingPlayerIds,
            onBidTapped = onBidTapped,
        )
    }
}

@Composable
private fun BoxScope.PlayerOfferListForState(
    offers: Loadable<List<PlayerOffer>>,
    emptyMessage: String,
    onPlayerTapped: (Int) -> Unit,
    onRejectTapped: (PlayerOffer) -> Unit,
    onAcceptTapped: (PlayerOffer) -> Unit,
) {
    when (offers) {
        is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is Loadable.Failed -> Text(
            text = emptyMessage,
            modifier = Modifier.align(Alignment.Center).padding(16.dp)
        )
        is Loadable.Success -> PlayerOfferList(
            offers = offers.value.sortedWith(PlayerOfferOrder),
            onPlayerTapped = onPlayerTapped,
            onRejectTapped = onRejectTapped,
            onAcceptTapped = onAcceptTapped,
        )
    }
}

@Composable
private fun BoxScope.PlayerBidListForState(
    bids: Loadable<List<PlayerBid>>,
    emptyMessage: String,
    onPlayerTapped: (Int) -> Unit,
    onRemoveTapped: (PlayerBid) -> Unit,
    removingBidIds: Set<Long>,
) {
    when (bids) {
        is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is Loadable.Failed -> Text(
            text = emptyMessage,
            modifier = Modifier.align(Alignment.Center).padding(16.dp)
        )
        is Loadable.Success -> PlayerBidList(
            bids = bids.value.sortedWith(PlayerBidOrder),
            onPlayerTapped = onPlayerTapped,
            onRemoveTapped = onRemoveTapped,
            removingBidIds = removingBidIds,
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
private fun MarketListingList(
    listings: List<MarketListing>,
    onPlayerTapped: (Int) -> Unit,
    onUnlistTapped: ((MarketListing) -> Unit)? = null,
    unlistingPlayerIds: Set<Int> = emptySet(),
    onBidTapped: ((MarketListing) -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listings) { listing ->
            MarketListingRow(
                listing = listing,
                onClick = { onPlayerTapped(listing.id) },
                onUnlist = onUnlistTapped?.let { { it(listing) } },
                unlisting = listing.id in unlistingPlayerIds,
                onBid = onBidTapped?.let { { it(listing) } },
            )
        }
    }
}

// My Listings passes onUnlist, Current Market passes onBid instead
// (never both — a listing is either mine to unlist or someone else's to
// bid on) — the same Box + overlay pattern PlayerOfferRow uses, so the
// button can sit bottom-end without disturbing the header/content/
// footer Column. onBid only opens the confirmation dialog (per
// AGENT.md's "no automated bidding without explicit confirmation"), so
// unlike onUnlist it takes no in-flight flag of its own — the spinner
// lives on the dialog's own confirm button instead.
@Composable
private fun MarketListingRow(
    listing: MarketListing,
    onClick: () -> Unit,
    onUnlist: (() -> Unit)? = null,
    unlisting: Boolean = false,
    onBid: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
    ) {
        Column(modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp)) {
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
        if (onUnlist != null) {
            PlayerOfferActionButton(
                icon = Icons.Default.Close,
                tint = TrendDown,
                contentDescription = "Unlist ${listing.name}",
                onClick = onUnlist,
                loading = unlisting,
                modifier = Modifier.align(Alignment.BottomEnd).padding(NocturneRadius.md),
            )
        }
        if (onBid != null) {
            PlayerOfferActionButton(
                icon = Icons.Default.Gavel,
                tint = AcceptGreen,
                contentDescription = "Place bid on ${listing.name}",
                onClick = onBid,
                modifier = Modifier.align(Alignment.BottomEnd).padding(NocturneRadius.md),
            )
        }
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
private fun PlayerOfferList(
    offers: List<PlayerOffer>,
    onPlayerTapped: (Int) -> Unit,
    onRejectTapped: (PlayerOffer) -> Unit,
    onAcceptTapped: (PlayerOffer) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(offers) { offer ->
            PlayerOfferRow(
                offer = offer,
                onClick = { onPlayerTapped(offer.id) },
                onReject = { onRejectTapped(offer) },
                onAccept = { onAcceptTapped(offer) },
            )
        }
    }
}

// Same header/content/footer shape as MarketListingRow, with the fields
// that differ for an offer: no expiry (an offer doesn't have one, unlike
// a listing), amount instead of an asking price.
@Composable
private fun PlayerOfferRow(offer: PlayerOffer, onClick: () -> Unit, onReject: () -> Unit, onAccept: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
    ) {
        Column(modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp)) {
            PlayerOfferHeader(offer = offer)
            PlayerOfferContent(offer = offer)
            PlayerOfferFooter(offer = offer)
        }
        // Accept alongside (not replacing) reject, overlaid bottom-right
        // of the card — the row itself is padded clear of the card's own
        // corner radius so neither button is clipped by it.
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).padding(NocturneRadius.md),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PlayerOfferActionButton(
                icon = Icons.Default.Check,
                tint = AcceptGreen,
                contentDescription = "Accept offer for ${offer.name}",
                onClick = onAccept,
            )
            PlayerOfferActionButton(
                icon = Icons.Default.Close,
                tint = TrendDown,
                contentDescription = "Reject offer for ${offer.name}",
                onClick = onReject,
            )
        }
    }
}

// Same color schema as the squad screen's status icons
// (SquadPlayerStatusIcon) — a tinted low-alpha background behind a
// full-opacity colored glyph, not a solid fill. Built as a plain
// clickable Box (not IconButton) so the whole circle is the tap target,
// not just the glyph.
@Composable
private fun PlayerOfferActionButton(
    icon: ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.24f))
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // No-dialog actions (unlist) swap the glyph for a spinner on this
        // same button while in flight, instead of a confirmation popup —
        // dialog-gated actions (reject/accept) never pass loading = true.
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = tint)
        } else {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(14.dp))
        }
    }
}

// Shared by both offer actions (reject/accept) — only the title, action
// label, and action color differ, per the accept-an-offer backlog entry
// ("same design"). A second use case is what earns this the shared
// composable, rather than a copy-pasted AcceptOfferDialog.
@Composable
private fun PlayerOfferConfirmationDialog(
    offer: PlayerOffer,
    title: String,
    actionLabel: String,
    actionColor: Color,
    inFlight: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!inFlight) onCancel() },
        // Default AlertDialog width is content-driven; force it to 90% of
        // the available width instead.
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(fraction = 0.9f),
        title = { Text(title) },
        text = {
            val (icon, color) = priceTrend(offer.amount - offer.price)
            // Player and quantities share one card surface now, instead
            // of the player floating above it.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NocturneRadius.md))
                    .background(ColorSurface)
                    .padding(32.dp),
            ) {
                PlayerAvatarWithPoints(
                    photoUrl = offer.photoUrl,
                    teamCrestUrl = offer.teamCrestUrl,
                    contentDescription = offer.name,
                    points = offer.points,
                )
                Text(
                    offer.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    PlayerOfferDialogRow(label = "Market value", value = formatPrice(offer.price))
                    PlayerOfferDialogRow(
                        label = "Offer",
                        value = formatPrice(offer.amount),
                        valueColor = ColorText,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    PlayerOfferDialogRow(
                        label = "Difference",
                        value = "$icon ${formatPriceChange(offer.amount - offer.price)}",
                        valueColor = color,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        },
        // Both buttons live in one row rather than the default
        // dismiss/confirm slots — at this dialog's forced 90% width,
        // those slots spread apart instead of matching this layout. Cancel
        // (the "do nothing" option) sits at the opposite end from the
        // action button, so they can't be mistaken for each other.
        confirmButton = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                // Same tinted-background/full-opacity-text treatment as
                // PositionTag and the player card's action buttons: a
                // translucent fill behind full-strength colored text,
                // rather than a solid button — purple (Nocturne's
                // ColorAccent/primary) for this general-purpose action.
                Button(
                    onClick = onCancel,
                    enabled = !inFlight,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("Cancel")
                }
                // Same translucent treatment as the matching action
                // button on the player card, in the same color.
                Button(
                    onClick = onConfirm,
                    enabled = !inFlight,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionColor.copy(alpha = 0.24f),
                        contentColor = actionColor,
                    ),
                ) {
                    if (inFlight) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = actionColor,
                        )
                    } else {
                        Text(actionLabel)
                    }
                }
            }
        },
    )
}

// Same shape as PlayerOfferConfirmationDialog (player card, label/value
// rows, Cancel/action button row) plus an editable amount field — the
// one dialog in the app that takes free-text input, since a bid amount
// isn't a fixed/server-decided number like a listing's price is. The
// amount itself lives in this composable's own remember{} state, not
// the Registry — only the final validated Long becomes onConfirm's
// payload, same reasoning MarketScreen's own selectedSubTab uses for
// ephemeral view-only state.
@Composable
private fun PlaceBidDialog(
    listing: MarketListing,
    inFlight: Boolean,
    onCancel: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var amountText by remember(listing.id) { mutableStateOf(listing.price.toString()) }
    val amount = amountText.toLongOrNull()
    val amountValid = amount != null && amount > 0

    AlertDialog(
        onDismissRequest = { if (!inFlight) onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(fraction = 0.9f),
        title = { Text("Place bid?") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NocturneRadius.md))
                    .background(ColorSurface)
                    .padding(32.dp),
            ) {
                PlayerAvatarWithPoints(
                    photoUrl = listing.photoUrl,
                    teamCrestUrl = listing.teamCrestUrl,
                    contentDescription = listing.name,
                    points = listing.points,
                )
                Text(
                    listing.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    PlayerOfferDialogRow(label = "Asking price", value = formatPrice(listing.price))
                    PlayerOfferDialogRow(
                        label = "Market value",
                        value = formatPrice(listing.marketValue),
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit) },
                    label = { Text("Your bid") },
                    singleLine = true,
                    enabled = !inFlight,
                    isError = amountText.isNotEmpty() && !amountValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                )
                if (amountValid) {
                    val (icon, color) = priceTrend(amount - listing.marketValue)
                    PlayerOfferDialogRow(
                        label = "Difference",
                        value = "$icon ${formatPriceChange(amount - listing.marketValue)}",
                        valueColor = color,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        },
        // Same forced-row Cancel/action layout as
        // PlayerOfferConfirmationDialog, for the same 90%-width reason.
        confirmButton = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCancel,
                    enabled = !inFlight,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { amount?.let(onConfirm) },
                    enabled = !inFlight && amountValid,
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcceptGreen.copy(alpha = 0.24f),
                        contentColor = AcceptGreen,
                    ),
                ) {
                    if (inFlight) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AcceptGreen,
                        )
                    } else {
                        Text("Bid")
                    }
                }
            }
        },
    )
}

// Label/value pair laid out like a table row: label left-aligned, value
// right-aligned in its own column, so amounts line up down the dialog
// regardless of label length.
@Composable
private fun PlayerOfferDialogRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(text = label, color = Neutral500, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
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
private fun PlayerBidList(
    bids: List<PlayerBid>,
    onPlayerTapped: (Int) -> Unit,
    onRemoveTapped: (PlayerBid) -> Unit,
    removingBidIds: Set<Long>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bids) { bid ->
            PlayerBidRow(
                bid = bid,
                onClick = { onPlayerTapped(bid.id) },
                onRemove = { onRemoveTapped(bid) },
                removing = bid.offerId in removingBidIds,
            )
        }
    }
}

// Same header/footer shape as MarketListingRow (owner/expiry header,
// market-value footer) — a bid's player has both, same as a listing's.
// The content differs: three numbers instead of one, since a bid has an
// asking price *and* my own offer against it, not just one price. Same
// Box + overlay pattern as MarketListingRow's onUnlist button — no
// confirmation dialog gates it either, per the backlog.
@Composable
private fun PlayerBidRow(bid: PlayerBid, onClick: () -> Unit, onRemove: () -> Unit, removing: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
    ) {
        Column(modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp)) {
            PlayerBidHeader(bid = bid)
            PlayerBidContent(bid = bid)
            PlayerBidFooter(bid = bid)
        }
        PlayerOfferActionButton(
            icon = Icons.Default.Close,
            tint = TrendDown,
            contentDescription = "Remove bid on ${bid.name}",
            onClick = onRemove,
            loading = removing,
            modifier = Modifier.align(Alignment.BottomEnd).padding(NocturneRadius.md),
        )
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

// Why a player can't be listed right now, or null if they can. Doesn't
// consider a pending offer a blocker — Squad's own "Listable ..." label
// only ever cares about the transfer lock, never about offers, and
// tapping List on a player mid-offer works fine in practice.
private fun listingIneligibilityReason(player: SquadPlayer, currentListingsCount: Int): String? = when {
    player.inMarket -> "Already listed"
    player.lockedUntil != null -> "Listable ${formatRelativeTime(player.lockedUntil)}"
    currentListingsCount >= MAX_LISTINGS -> "Listing cap reached"
    else -> null
}

// list-a-player's popup: the whole squad as cards, sized to ~90%/94% of
// the screen (same forced-sizing technique PlayerOfferConfirmationDialog
// uses for its 90% width), on the app's own background rather than the
// default dialog surface color. Eligible players lead, ineligible ones
// trail (each group still in position order) — no dialog-specific state
// beyond which ids are currently in flight.
@Composable
private fun ListPlayerPopup(
    squad: Loadable<List<SquadPlayer>>,
    currentListingsCount: Int,
    listingPlayerIds: Set<Int>,
    onDismiss: () -> Unit,
    onListTapped: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(fraction = 0.9f).fillMaxHeight(fraction = 0.94f),
        containerColor = ColorBg,
        title = { Text("List a player") },
        text = {
            when (squad) {
                is Loadable.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is Loadable.Failed -> Text("Could not load your squad right now.")
                is Loadable.Success -> {
                    val candidates = squad.value
                        .map { player -> player to listingIneligibilityReason(player, currentListingsCount) }
                        .sortedWith(compareBy({ (_, reason) -> reason != null }, { (player, _) -> player.positionSortRank }))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(candidates) { (player, reason) ->
                            SquadListingCandidateCard(
                                player = player,
                                ineligibilityReason = reason,
                                listing = player.id in listingPlayerIds,
                                onClick = { onListTapped(player.id) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Close")
            }
        },
    )
}

// Same avatar/name/position-tag + market-value/trend shape as
// MarketListingRow's content, plus a footer with the acquisition price
// (same drafted/signed fallback SquadScreen.kt's SquadPlayerRow already
// uses) — a feature-local card, not shared with LineupScreen's
// BenchCandidateCard, since the fields differ enough (market value/trend
// + acquisition price here, vs. just points/position there) that sharing
// isn't a clean fit yet. `ineligibilityReason != null` dims the card,
// drops its click target, and shows the reason top-left, rather than
// hiding the card outright — same reasoning as BenchCandidateCard's
// `enabled`. `listing = true` (this card's write in flight) dims it
// further and shows a centered spinner instead of reacting to taps — no
// confirmation dialog, same as unlist-a-player.
@Composable
private fun SquadListingCandidateCard(
    player: SquadPlayer,
    ineligibilityReason: String?,
    listing: Boolean,
    onClick: () -> Unit,
) {
    val eligible = ineligibilityReason == null
    val tappable = eligible && !listing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .alpha(if (eligible) 1f else 0.4f)
            .then(if (tappable) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        val drafted = player.signedPrice == null
        val forLabel = if (drafted) "Drafted at" else "Signed for"
        val forAmount = if (drafted) player.draftedPrice else player.signedPrice

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            ineligibilityReason?.let { reason ->
                Text(
                    text = reason,
                    fontSize = 13.sp,
                    color = Neutral500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (ineligibilityReason != null) Modifier.padding(top = 8.dp) else Modifier,
            ) {
                PlayerAvatarWithPoints(
                    photoUrl = player.photoUrl,
                    teamCrestUrl = player.teamCrestUrl,
                    contentDescription = player.name,
                    points = player.points,
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(text = player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        PositionTag(position = player.position, secondaryPosition = player.secondaryPosition)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = formatPrice(player.price), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
                    val (icon, color) = priceTrend(player.priceIncrement)
                    Text(text = "$icon ${formatPriceChange(player.priceIncrement)}", fontSize = 12.sp, color = color)
                }
            }
            forAmount?.let { amount ->
                Text(
                    text = "$forLabel: ${formatPrice(amount)}",
                    fontSize = 13.sp,
                    color = Neutral500,
                    modifier = Modifier.padding(top = 8.dp + PlayerAvatarOverlayOffsetY),
                )
            }
        }
        if (listing) {
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}
