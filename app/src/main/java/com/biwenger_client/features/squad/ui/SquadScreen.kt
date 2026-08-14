package com.biwenger_client.features.squad.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.features.squad.domain.models.Player
import java.text.NumberFormat
import java.util.Locale

private val POSITION_LABELS = mapOf(1 to "GK", 2 to "DF", 3 to "MF", 4 to "FW")

@Composable
fun SquadScreen(
    viewModel: SquadViewModel = hiltViewModel()
) {
    val players by viewModel.players
    SquadScreen(players = players)
}

@Composable
private fun SquadScreen(
    players: Loadable<List<Player>>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (players) {
            is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is Loadable.Failed -> Text(
                text = "Could not load your squad right now.",
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
            is Loadable.Success -> PlayerList(players = players.value)
        }
    }
}

@Composable
private fun PlayerList(players: List<Player>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(players) { player ->
            PlayerRow(player = player)
            Divider()
        }
    }
}

@Composable
private fun PlayerRow(player: Player) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = player.name)
        Row {
            Text(text = POSITION_LABELS[player.position] ?: player.position.toString())
            Text(text = "  " + formatPrice(player.price))
        }
    }
}

private fun formatPrice(price: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(price)
