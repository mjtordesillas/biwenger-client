package com.biwenger_client.features.lineup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.ui.FootballPitch
import com.biwenger_client.ui.PositionColors
import com.biwenger_client.ui.theme.Neutral900

// The same translucent green a midfielder's PositionTag already uses,
// not a new bespoke pitch color — the turf reuses the palette that's
// already on screen (player pills) rather than adding one just for
// this. Lines at full opacity, unlike a PositionTag's fill, so the
// markings stay crisp against it.
private val PitchGreen = PositionColors.getValue(3).copy(alpha = 0.24f)
private val PitchLineColor = Color.White

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

// Every player is placed at an absolute (x, y) fraction of the pitch
// rather than laid out in a Row per position band — the midfield curve
// needs specific, non-band-relative height targets (see below), and
// absolute placement is also what keeps wide rows (5 midfielders) from
// ever overflowing the pitch horizontally, unlike fixed-width Row
// children whose total width Row can't shrink to fit.
//
// Y fractions are top-down (0 = goal line at the top/forwards' end, 1 =
// goal line at the bottom/goalkeeper's end) and mark where the BOTTOM
// of a player's name pill lands, not the top of their photo.
private const val ForwardYFraction = 0.16f

// A central midfielder dips to 40% "up the pitch" from their own goal
// (60% top-down); the widest midfielders sit at 70% up the pitch (30%
// top-down) — everyone between interpolated by the same parabola the
// row's width uses.
private const val MidfielderEdgeYFraction = 0.30f
private const val MidfielderCenterYFraction = 0.60f
private const val DefenderYFraction = 0.80f
private const val GoalkeeperYFraction = 0.95f

// Fixed regardless of avatar/pill content, so placement math (and the
// x-spacing below) doesn't depend on how long a name is.
private val PitchPlayerWidth = 56.dp
private val PitchPlayerHeight = 58.dp

// Forwards nearest the top (closest to goal, attacking direction is
// "up"), then midfielders, defenders, goalkeeper at the bottom —
// grouped by each player's own `position`, not by parsing `formation`
// or trusting list order (see docs/biwenger-api-notes.md § "Starting
// lineup").
@Composable
private fun PitchLineup(players: List<Player>, modifier: Modifier = Modifier) {
    val byPosition = players.groupBy { it.position }
    BoxWithConstraints(modifier = modifier) {
        PitchPositionRow(players = byPosition[4].orEmpty(), pitchSize = DpSize(maxWidth, maxHeight)) { _, _ -> ForwardYFraction }
        PitchPositionRow(players = byPosition[3].orEmpty(), pitchSize = DpSize(maxWidth, maxHeight), yFraction = ::midfielderYFraction)
        PitchPositionRow(players = byPosition[2].orEmpty(), pitchSize = DpSize(maxWidth, maxHeight)) { _, _ -> DefenderYFraction }
        PitchPositionRow(players = byPosition[1].orEmpty(), pitchSize = DpSize(maxWidth, maxHeight)) { _, _ -> GoalkeeperYFraction }
    }
}

// Level (all at MidfielderEdgeYFraction) for one or two players —
// nothing to curve. Three or more bows into a shallow valley: the
// widest pair sit highest (shallowest), whoever's in the middle
// dips deepest, same shape a real midfield line reads as on a tactics
// board. Parabola: 0 at both edges (t=0, t=1), 1 at the center (t=0.5).
private fun midfielderYFraction(index: Int, count: Int): Float {
    val t = if (count > 1) index / (count - 1).toFloat() else 0.5f
    val curveFraction = if (count > 2) 4f * t * (1f - t) else 0f
    return MidfielderEdgeYFraction + (MidfielderCenterYFraction - MidfielderEdgeYFraction) * curveFraction
}

@Composable
private fun PitchPositionRow(
    players: List<Player>,
    pitchSize: DpSize,
    yFraction: (index: Int, count: Int) -> Float,
) {
    players.forEachIndexed { index, player ->
        // Evenly spaced, strictly between the pitch's edges (never at
        // 0 or 1), so a wide row never touches the sideline.
        val xFraction = (index + 1) / (players.size + 1f)
        val centerX = pitchSize.width * xFraction
        val bottomY = pitchSize.height * yFraction(index, players.size)
        PitchPlayer(
            player = player,
            modifier = Modifier
                .width(PitchPlayerWidth)
                .offset(x = centerX - PitchPlayerWidth / 2, y = bottomY - PitchPlayerHeight)
        )
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
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = player.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = PitchPlayerWidth)
                .clip(RoundedCornerShape(percent = 50))
                .background(Neutral900)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
