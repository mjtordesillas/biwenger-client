package com.biwenger_client.features.squad.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.ui.FilterChip
import com.biwenger_client.ui.MatchDayDetailsScreen
import com.biwenger_client.ui.PlayerDetailScreen
import com.biwenger_client.ui.PlayerList
import com.biwenger_client.ui.PositionColors
import com.biwenger_client.ui.PositionLabels

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
    players: Loadable<List<Player>>,
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
    val filteredPlayers = allPlayers.filter { selectedPosition == null || it.position == selectedPosition }
    val selectedPlayer = allPlayers.find { it.id == selectedPlayerId }

    // Exclusive, not overlaid: only one screen is ever composed at a
    // time, so there's nothing behind it for an unclaimed tap to fall
    // through to.
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
                    is Loadable.Success -> PlayerList(players = filteredPlayers, onPlayerTapped = onPlayerTapped)
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
