package com.biwenger_client.features.squad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.domain.models.SquadPlayer
import com.biwenger_client.ui.FilterChip
import com.biwenger_client.ui.MatchDayDetailsScreen
import com.biwenger_client.ui.PlayerAvatarOverlayOffsetY
import com.biwenger_client.ui.PlayerAvatarWithPoints
import com.biwenger_client.ui.PlayerDetailScreen
import com.biwenger_client.ui.PositionColors
import com.biwenger_client.ui.PositionLabels
import com.biwenger_client.ui.PositionTag
import com.biwenger_client.ui.PriceTrend
import com.biwenger_client.ui.StatusDoubt
import com.biwenger_client.ui.StatusInjured
import com.biwenger_client.ui.TrendDown
import com.biwenger_client.ui.TrendUp
import com.biwenger_client.ui.formatDate
import com.biwenger_client.ui.formatPrice
import com.biwenger_client.ui.formatRelativeTime
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral500
import com.biwenger_client.ui.theme.NocturneRadius

private val FilterPositions = listOf(null, 1, 2, 3, 4) // null = All

@Composable
fun SquadScreen(
    viewModel: SquadViewModel = hiltViewModel()
) {
    val players by viewModel.players
    val selectedPosition by viewModel.selectedPosition
    val selectedPlayerId by viewModel.selectedPlayerId
    val priceHistory by viewModel.priceHistory
    val performanceHistory by viewModel.performanceHistory
    val performanceHistorySeason by viewModel.performanceHistorySeason
    val selectedMatchDay by viewModel.selectedMatchDay
    val matchDayDetails by viewModel.matchDayDetails

    SquadScreen(
        players = players,
        selectedPosition = selectedPosition,
        selectedPlayerId = selectedPlayerId,
        priceHistory = priceHistory,
        performanceHistory = performanceHistory,
        performanceHistorySeason = performanceHistorySeason,
        selectedMatchDay = selectedMatchDay,
        matchDayDetails = matchDayDetails,
        onPositionSelected = viewModel::positionFilterChanged,
        onPlayerTapped = viewModel::playerTapped,
        onPerformanceSeasonChanged = viewModel::performanceSeasonChanged,
        onSheetDismissed = viewModel::sheetClosed,
        onMatchDayTapped = viewModel::matchDayTapped,
        onMatchDayDetailsDismissed = viewModel::matchDayDetailsClosed,
    )
}

@Composable
private fun SquadScreen(
    players: Loadable<List<SquadPlayer>>,
    selectedPosition: Int?,
    selectedPlayerId: Int?,
    priceHistory: Loadable<PriceHistory>?,
    performanceHistory: Loadable<PerformanceHistory>?,
    performanceHistorySeason: String,
    selectedMatchDay: Int?,
    matchDayDetails: Loadable<MatchDayDetails>?,
    onPositionSelected: (Int?) -> Unit,
    onPlayerTapped: (Int) -> Unit,
    onPerformanceSeasonChanged: (Int, String) -> Unit,
    onSheetDismissed: () -> Unit,
    onMatchDayTapped: (Int, Int, String) -> Unit,
    onMatchDayDetailsDismissed: () -> Unit,
) {
    val allPlayers = (players as? Loadable.Success)?.value.orEmpty()
    val filteredPlayers = allPlayers
        .filter { selectedPosition == null || it.position == selectedPosition }
        .sortedBy { it.positionSortRank }
    val selectedPlayer = allPlayers.find { it.id == selectedPlayerId }

    // Exclusive, not overlaid: only one screen is ever composed at a
    // time, so there's nothing behind it for an unclaimed tap to fall
    // through to.
    if (selectedPlayer != null && selectedMatchDay != null) {
        MatchDayDetailsScreen(player = selectedPlayer.toPlayer(), matchDayDetails = matchDayDetails, onBack = onMatchDayDetailsDismissed)
    } else if (selectedPlayer != null) {
        PlayerDetailScreen(
            player = selectedPlayer.toPlayer(),
            priceHistory = priceHistory,
            performanceHistory = performanceHistory,
            performanceHistorySeason = performanceHistorySeason,
            onPerformanceSeasonChanged = { season -> onPerformanceSeasonChanged(selectedPlayer.id, season) },
            onMatchDayTapped = { matchDay -> onMatchDayTapped(selectedPlayer.id, matchDay, performanceHistorySeason) },
            onBack = onSheetDismissed
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            SquadHeader()
            PositionFilterRow(selectedPosition = selectedPosition, onPositionSelected = onPositionSelected)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (players) {
                    is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is Loadable.Failed -> Text(
                        text = "Could not load your squad right now.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    is Loadable.Success -> SquadPlayerList(players = filteredPlayers, onPlayerTapped = onPlayerTapped)
                }
            }
        }
    }
}

@Composable
private fun SquadHeader() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = "My Squad", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun PositionFilterRow(selectedPosition: Int?, onPositionSelected: (Int?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPositions.forEach { position ->
            val active = position == selectedPosition
            val color = position?.let { PositionColors[it] } ?: MaterialTheme.colorScheme.primary
            FilterChip(
                label = position?.let { PositionLabels[it] } ?: "All",
                color = color,
                active = active,
                onClick = { onPositionSelected(position) }
            )
        }
    }
}

@Composable
private fun SquadPlayerList(players: List<SquadPlayer>, onPlayerTapped: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(players) { player -> SquadPlayerRow(player = player, onClick = { onPlayerTapped(player.id) }) }
    }
}

// Same header/content/footer shape as Market's MarketListingRow: a
// header line about ownership (listable status left, signed/drafted-on
// date right — every player has both, so the header always renders,
// unlike Market's conditional one), the avatar/name/price content, then
// a footer (signed/drafted-at price left, status icons right).
@Composable
private fun SquadPlayerRow(player: SquadPlayer, onClick: () -> Unit) {
    // Draft-owned (never bought via the market) gets "Drafted"
    // wording and the market value on the draft date in place of what
    // was actually paid, which doesn't apply to a draft pick.
    val drafted = player.signedPrice == null
    val onLabel = if (drafted) "Drafted on" else "Signed on"
    val forLabel = if (drafted) "Drafted at" else "Signed for"
    val forAmount = if (drafted) player.draftedPrice else player.signedPrice

    val listableLabel = player.lockedUntil?.let { "Listable ${formatRelativeTime(it)}" } ?: "Listable"
    val signedForLabel = forAmount?.let { "$forLabel: ${formatPrice(it)}" }
    val statusIcons = squadPlayerStatusIcons(player)
    val hasFooter = signedForLabel != null || statusIcons.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = listableLabel,
                fontSize = 13.sp,
                color = Neutral500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$onLabel: ${formatDate(player.signedAt)}",
                fontSize = 13.sp,
                color = Neutral500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatarWithPoints(
                photoUrl = player.photoUrl,
                teamCrestUrl = player.teamCrestUrl,
                contentDescription = player.name,
                points = player.points
            )

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(text = player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    PositionTag(position = player.position, secondaryPosition = player.secondaryPosition)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatPrice(player.price), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
                PriceTrend(priceIncrement = player.priceIncrement)
            }
        }

        if (hasFooter) {
            // Top padding is content's 8dp plus the points badge/crest
            // overhang (PlayerAvatarOverlayOffsetY) — that overlay pokes
            // past the content row's layout bounds without adding to its
            // measured height, so matching 8dp exactly here would look
            // tighter than the header-to-content gap. Same reasoning as
            // Market's MarketListingFooter.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp + PlayerAvatarOverlayOffsetY),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = signedForLabel.orEmpty(),
                    fontSize = 13.sp,
                    color = Neutral500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    statusIcons.forEach { statusIcon -> StatusIconBadge(statusIcon = statusIcon) }
                }
            }
        }
    }
}

private data class SquadPlayerStatusIcon(val icon: ImageVector, val color: Color, val contentDescription: String)

// Order matches how urgent/actionable each fact is: fitness first (it
// affects whether to even start this player), then the offer (is it
// worth taking), then "already listed" last (already acted on).
private fun squadPlayerStatusIcons(player: SquadPlayer): List<SquadPlayerStatusIcon> = buildList {
    if (player.status == "injured") add(SquadPlayerStatusIcon(Icons.Default.Close, StatusInjured, "Injured"))
    if (player.status == "doubt") add(SquadPlayerStatusIcon(Icons.Default.QuestionMark, StatusDoubt, "Doubt for the next match"))
    player.offerAmount?.let { offerAmount ->
        // >= counts as "above value" — no offer sample has landed exactly
        // on the market value to motivate a third (flat) treatment.
        val aboveValue = offerAmount >= player.price
        add(
            SquadPlayerStatusIcon(
                icon = Icons.Default.AttachMoney,
                color = if (aboveValue) TrendUp else TrendDown,
                contentDescription = if (aboveValue) "Offer above market value" else "Offer below market value"
            )
        )
    }
    if (player.inMarket) add(SquadPlayerStatusIcon(Icons.Default.Storefront, Neutral500, "Listed on the market"))
}

// Same pill treatment as PositionTag — tinted low-alpha background,
// colored glyph instead of white-on-solid — just an icon instead of
// text, sized to sit inside one text line rather than tower over it.
@Composable
private fun StatusIconBadge(statusIcon: SquadPlayerStatusIcon) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(NocturneRadius.md * 0.75f))
            .background(statusIcon.color.copy(alpha = 0.24f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = statusIcon.icon,
            contentDescription = statusIcon.contentDescription,
            tint = statusIcon.color,
            modifier = Modifier.size(10.dp)
        )
    }
}
