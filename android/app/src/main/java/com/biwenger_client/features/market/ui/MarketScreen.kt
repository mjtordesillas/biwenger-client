package com.biwenger_client.features.market.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.biwenger_client.ui.PlayerList

// Slice 1: list only — who's currently for sale and at what price.
// No expiry, seller, or balance/maxBid yet (see docs/backlog for the
// deferred slices); no tap interaction either, unlike SquadScreen's
// player rows — there's nothing to show in a detail sheet yet that
// isn't already on the row.
@Composable
fun MarketScreen(
    viewModel: MarketViewModel = hiltViewModel()
) {
    val players by viewModel.players
    MarketScreen(players = players)
}

@Composable
private fun MarketScreen(players: Loadable<List<Player>>) {
    Column(modifier = Modifier.fillMaxSize()) {
        MarketHeader()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (players) {
                is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Loadable.Failed -> Text(
                    text = "Could not load the market right now.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is Loadable.Success -> PlayerList(players = players.value, onPlayerTapped = {})
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
