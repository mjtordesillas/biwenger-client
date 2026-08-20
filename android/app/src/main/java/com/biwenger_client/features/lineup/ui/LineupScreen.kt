package com.biwenger_client.features.lineup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val saveError by viewModel.saveError
    LineupScreen(lineup = lineup, saveError = saveError, onVacate = viewModel::vacateSlot)
}

@Composable
private fun LineupScreen(lineup: Loadable<Lineup>, saveError: Boolean, onVacate: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (lineup) {
            is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is Loadable.Failed -> Text(
                text = "Could not load your lineup right now.",
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
            is Loadable.Success -> LineupContent(lineup = lineup.value, saveError = saveError, onVacate = onVacate)
        }
    }
}

@Composable
private fun LineupContent(lineup: Lineup, saveError: Boolean, onVacate: (Int) -> Unit) {
    // Tap a starter, confirm in the dialog below — the "save/confirm
    // flow" docs/backlog/to-do/swap-lineup-players.md calls for. Purely
    // local UI state (which player, if any, the dialog is asking
    // about), not store-backed: nothing here needs a coeffect or
    // survives navigating away.
    var playerToVacate by remember { mutableStateOf<Player?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = lineup.formation,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (saveError) {
            Text(
                text = "Could not save your lineup right now.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }

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
            PitchLineup(
                players = lineup.players,
                formation = lineup.formation,
                onPlayerTap = { player -> playerToVacate = player },
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )
        }
    }

    playerToVacate?.let { player ->
        VacateSlotDialog(
            player = player,
            onConfirm = {
                onVacate(player.id)
                playerToVacate = null
            },
            onDismiss = { playerToVacate = null }
        )
    }
}

@Composable
private fun VacateSlotDialog(player: Player, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bench ${player.name}?") },
        text = { Text("They'll be removed from the lineup with no replacement.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Bench") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Calibrated against a 24-column x 20-row grid overlaid on the pitch
// during design: vertical reference is the BOTTOM of the name pill,
// horizontal reference is the CENTER of the player. Row/column values
// are grid line indices (row 0 = forwards' goal line, row 20 =
// goalkeeper's; column 0 = left edge, column 24 = right edge).
private data class PositionBand(val highestRow: Int, val lowestRow: Int)

private val ForwardBand = PositionBand(highestRow = 3, lowestRow = 5)
private val MidfielderBand = PositionBand(highestRow = 8, lowestRow = 10)
private val DefenderBand = PositionBand(highestRow = 14, lowestRow = 16)
private const val GoalkeeperRow = 20

private const val GridColumns = 24
private const val GridRows = 20
private const val CenterColumn = 12
private const val MaxHalfSpanColumns = 9 // the fixed curve's edges: columns 3 and 21
private const val PairHalfSpanColumns = 5f // exactly 2 players: centers on columns 7 and 17

// Forwards nearest the top (closest to goal, attacking direction is
// "up"), then midfielders, defenders, goalkeeper at the bottom —
// sliced off `players` IN ORDER (goalkeeper, then `formation`'s
// defender/midfielder/forward counts, back-to-front — see
// docs/biwenger-api-notes.md § "Starting lineup"), NOT grouped by each
// player's own catalogue `position`. Those two disagree whenever a
// player is aligned in their secondary position — Biwenger lets a
// manager play e.g. a MF/FW player as a forward for extra credits, and
// at that point their catalogue position is stale for "where do they
// actually stand on this pitch". `players`' order is the only thing
// that still reflects the real alignment.
@Composable
private fun PitchLineup(
    players: List<Player?>,
    formation: String,
    onPlayerTap: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    val bands = sliceLineupBands(players, parseFormation(formation))
    BoxWithConstraints(modifier = modifier) {
        val pitchSize = DpSize(maxWidth, maxHeight)
        PitchPositionRow(players = bands.forwards, band = ForwardBand, pitchSize = pitchSize, onPlayerTap = onPlayerTap)
        PitchPositionRow(players = bands.midfielders, band = MidfielderBand, pitchSize = pitchSize, onPlayerTap = onPlayerTap)
        PitchPositionRow(players = bands.defenders, band = DefenderBand, pitchSize = pitchSize, onPlayerTap = onPlayerTap)
        PitchPositionRow(
            players = bands.goalkeepers,
            band = PositionBand(GoalkeeperRow, GoalkeeperRow),
            pitchSize = pitchSize,
            onPlayerTap = onPlayerTap
        )
    }
}

// "D-M-F" (e.g. "3-5-2") — goalkeeper is always exactly 1 and never
// part of the string, see docs/biwenger-api-notes.md § "Starting
// lineup".
data class FormationCounts(val defenders: Int, val midfielders: Int, val forwards: Int)

fun parseFormation(formation: String): FormationCounts {
    val counts = formation.split("-").mapNotNull { it.toIntOrNull() }
    return FormationCounts(
        defenders = counts.getOrElse(0) { 0 },
        midfielders = counts.getOrElse(1) { 0 },
        forwards = counts.getOrElse(2) { 0 },
    )
}

data class LineupBands(
    val goalkeepers: List<Player>,
    val defenders: List<Player>,
    val midfielders: List<Player>,
    val forwards: List<Player>,
)

// Slices `players` IN ORDER — goalkeeper, then `counts`' defender/
// midfielder/forward counts, back-to-front — rather than grouping by
// each player's own catalogue `position`. Those two disagree whenever
// a player is aligned in their secondary position: Biwenger lets a
// manager play e.g. a MF/FW player as a forward for extra credits, and
// at that point their catalogue position is stale for "where do they
// actually stand on this pitch" — `players`' order is the only thing
// that still reflects the real alignment (see
// docs/biwenger-api-notes.md § "Starting lineup"). A `null` entry
// (a vacant slot) is consumed as one element of whichever band it
// falls in, same as a real player, so it can't shift a later band's
// slice — only `withVacantSlots` turns it (or a genuine shortfall) into
// the placeholder PitchPlayer renders.
fun sliceLineupBands(players: List<Player?>, counts: FormationCounts): LineupBands {
    var remaining = players
    fun take(expectedCount: Int): List<Player> {
        val slice = remaining.take(expectedCount)
        remaining = remaining.drop(slice.size)
        return withVacantSlots(slice, expectedCount)
    }
    return LineupBands(
        goalkeepers = take(1),
        defenders = take(counts.defenders),
        midfielders = take(counts.midfielders),
        forwards = take(counts.forwards),
    )
}

// A "?" over Biwenger's own default player photo (see docs/
// biwenger-api-notes.md § "Image CDN") for any slot the formation
// expects but the lineup doesn't actually fill — either a `null` entry
// (a real vacancy, see docs/biwenger-api-notes.md § "Starting lineup —
// write") or the list simply running short (defensive; not the shape
// actually observed). Only `photoUrl`/`name` ever get read off this by
// PitchPlayer, so the rest of the fields are unused filler.
private const val VacantPlayerPhotoUrl = "https://cdn.biwenger.com/i/p/0.png"

private val VacantPlayer = Player(
    id = 0,
    name = "?",
    position = 0,
    secondaryPosition = null,
    price = 0,
    priceIncrement = 0,
    points = 0,
    photoUrl = VacantPlayerPhotoUrl,
    teamCrestUrl = "",
)

fun withVacantSlots(players: List<Player?>, expectedCount: Int): List<Player> {
    val shortfall = (expectedCount - players.size).coerceAtLeast(0)
    return (players + List(shortfall) { null }).map { it ?: VacantPlayer }
}

// One player: dead center, at the band's midpoint depth (no edge/
// center distinction with just one player). Two: level, at the band's
// midpoint depth, spread to a fixed 10-column gap (further apart than
// the curve below would put them, so a sparse row doesn't read as two
// players standing right on top of each other). Three or more sample N
// evenly-spaced points along ONE fixed curve — always column 3/highest
// to column 12/lowest to column 21/highest, the same physical curve
// regardless of count — rather than a count-scaled, narrower copy of
// it. A row with fewer than 5 players still spans the full width; it
// just has fewer, more sparsely sampled points along that curve, so
// its inner players land close to but not exactly at the lowest point.
@Composable
private fun PitchPositionRow(players: List<Player>, band: PositionBand, pitchSize: DpSize, onPlayerTap: (Player) -> Unit) {
    val count = players.size
    val midpointRow = (band.highestRow + band.lowestRow) / 2f
    val halfSpan = if (count == 2) PairHalfSpanColumns else MaxHalfSpanColumns.toFloat()

    players.forEachIndexed { index, player ->
        val t = if (count > 1) index / (count - 1).toFloat() else 0.5f
        val column = CenterColumn + (t - 0.5f) * (2 * halfSpan)
        val row = if (count <= 2) {
            midpointRow
        } else {
            val curveFraction = 4f * t * (1f - t)
            band.highestRow + (band.lowestRow - band.highestRow) * curveFraction
        }

        val centerX = pitchSize.width * (column / GridColumns)
        val bottomY = pitchSize.height * (row / GridRows)

        PitchPlayer(
            player = player,
            onTap = onPlayerTap,
            modifier = Modifier
                .width(PitchPlayerWidth)
                .offset(x = centerX - PitchPlayerWidth / 2, y = bottomY - PitchPlayerHeight)
        )
    }
}

private val PitchPlayerWidth = 88.dp
private val PitchPlayerHeight = 66.dp

private val PitchPlayerPhotoSize = 48.dp

// A vacant slot (id 0, see VacantPlayer above) isn't tappable — nothing
// to bench there.
@Composable
private fun PitchPlayer(player: Player, onTap: (Player) -> Unit, modifier: Modifier = Modifier) {
    val tapModifier = if (player.id == 0) modifier else modifier.clickable { onTap(player) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = tapModifier) {
        // The plain mugshot, not PlayerAvatar's circle-masked photo +
        // team crest — those make sense in a list row, not stood on a
        // pitch where the shirt/crest is already visually redundant.
        AsyncImage(
            model = player.photoUrl,
            contentDescription = player.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(PitchPlayerPhotoSize)
        )
        Text(
            text = player.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            // Never narrower than the photo above it, so a short name
            // (or "?" on a vacant slot) doesn't leave a pill visibly
            // thinner than the player it's labeling. Wider cap than a
            // straight row would risk — curved rows space players out
            // horizontally, so neighboring pills are less likely to
            // crowd each other.
            modifier = Modifier
                .widthIn(min = PitchPlayerPhotoSize, max = 88.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Neutral900)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
