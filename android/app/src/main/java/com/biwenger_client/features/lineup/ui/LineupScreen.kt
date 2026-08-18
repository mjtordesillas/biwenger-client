package com.biwenger_client.features.lineup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.ui.FootballPitch
import com.biwenger_client.ui.theme.Neutral900

// A green pitch is the one deliberate exception to Nocturne's "keep
// chroma low outside the accent" rule here — same carve-out reasoning
// as PlayerColors.kt's position/trend colors, just representational
// (a pitch has to read as a pitch) rather than functional.
private val PitchGreen = Color(0xFF1E5631)
private val PitchLineColor = Color.White.copy(alpha = 0.55f)

@Composable
fun LineupScreen(
    viewModel: LineupViewModel = hiltViewModel()
) {
    val lineup by viewModel.lineup
    LineupScreen(lineup = lineup)
}

@Composable
private fun LineupScreen(lineup: Loadable<Lineup>) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (lineup) {
            is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is Loadable.Failed -> Text(
                text = "Could not load your lineup right now.",
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
            is Loadable.Success -> LineupContent(lineup = lineup.value)
        }
    }
}

@Composable
private fun LineupContent(lineup: Lineup) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = lineup.formation,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            FootballPitch(
                modifier = Modifier.fillMaxSize(),
                lineColor = PitchLineColor,
                fillColor = PitchGreen
            )
            PitchLineup(players = lineup.players, modifier = Modifier.fillMaxSize().padding(12.dp))
        }
    }
}

// Forwards nearest the top (closest to goal, attacking direction is
// "up"), then midfielders, defenders, goalkeeper at the bottom —
// grouped by each player's own `position`, not by parsing `formation`
// or trusting list order (see docs/biwenger-api-notes.md § "Starting
// lineup"). Fixed four rows, even when a group is empty, so each
// position band always claims the same share of pitch height.
@Composable
private fun PitchLineup(players: List<Player>, modifier: Modifier = Modifier) {
    val byPosition = players.groupBy { it.position }
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
        listOf(4, 3, 2, 1).forEach { position ->
            PitchRow(players = byPosition[position].orEmpty(), modifier = Modifier.weight(1f))
        }
    }
}

// Level for one or two players — nothing to curve. Three or more bows
// into a shallow valley: edges sit highest (closest to the row's own
// top), the middle player(s) pushed down toward the row below, same
// shape a real back/midfield line reads as on a tactics board.
private val RowCurveDepth = 16.dp

@Composable
private fun PitchRow(players: List<Player>, modifier: Modifier = Modifier) {
    val curved = players.size > 2
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEachIndexed { index, player ->
            val t = if (players.size > 1) index / (players.size - 1).toFloat() else 0.5f
            // Parabola: 0 at both edges (t=0, t=1), 1 at the center (t=0.5).
            val curveFraction = 4f * t * (1f - t)
            PitchPlayer(
                player = player,
                modifier = if (curved) Modifier.offset(y = RowCurveDepth * curveFraction) else Modifier
            )
        }
    }
}

@Composable
private fun PitchPlayer(player: Player, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        // The plain mugshot, not PlayerAvatar's circle-masked photo +
        // team crest — those make sense in a list row, not stood on a
        // pitch where the shirt/crest is already visually redundant.
        AsyncImage(
            model = player.photoUrl,
            contentDescription = player.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = player.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 3.dp)
                .widthIn(max = 68.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Neutral900)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
