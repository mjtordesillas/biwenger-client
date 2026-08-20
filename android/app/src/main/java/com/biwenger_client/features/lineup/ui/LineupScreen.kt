package com.biwenger_client.features.lineup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.lineup.domain.models.BenchCandidates
import com.biwenger_client.features.lineup.domain.models.FreeFormations
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.features.squad.domain.models.SquadPlayer
import com.biwenger_client.ui.FootballPitch
import com.biwenger_client.ui.PlayerAvatarWithPoints
import com.biwenger_client.ui.PositionColors
import com.biwenger_client.ui.PositionTag
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral900
import com.biwenger_client.ui.theme.NocturneRadius

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
    val saving by viewModel.saving
    val slotPicker by viewModel.slotPicker
    LineupScreen(
        lineup = lineup,
        saveError = saveError,
        saving = saving,
        slotPicker = slotPicker,
        onVacate = viewModel::vacateSlot,
        onRequestBenchOptions = viewModel::requestBenchOptions,
        onFillSlot = viewModel::fillSlot,
        onClosePicker = viewModel::closeSlotPicker,
        onChangeFormation = viewModel::changeFormation,
    )
}

@Composable
private fun LineupScreen(
    lineup: Loadable<Lineup>,
    saveError: Boolean,
    saving: Boolean,
    slotPicker: Loadable<BenchCandidates>?,
    onVacate: (Int) -> Unit,
    onRequestBenchOptions: (LineupSlot) -> Unit,
    onFillSlot: (Int, Int) -> Unit,
    onClosePicker: () -> Unit,
    onChangeFormation: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (lineup) {
            is Loadable.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is Loadable.Failed -> Text(
                text = "Could not load your lineup right now.",
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
            is Loadable.Success -> LineupContent(
                lineup = lineup.value,
                saveError = saveError,
                saving = saving,
                slotPicker = slotPicker,
                onVacate = onVacate,
                onRequestBenchOptions = onRequestBenchOptions,
                onFillSlot = onFillSlot,
                onClosePicker = onClosePicker,
                onChangeFormation = onChangeFormation,
            )
        }
    }
}

@Composable
private fun LineupContent(
    lineup: Lineup,
    saveError: Boolean,
    saving: Boolean,
    slotPicker: Loadable<BenchCandidates>?,
    onVacate: (Int) -> Unit,
    onRequestBenchOptions: (LineupSlot) -> Unit,
    onFillSlot: (Int, Int) -> Unit,
    onClosePicker: () -> Unit,
    onChangeFormation: (String) -> Unit,
) {
    // Which slot the dialog below is open for, if any — purely local UI
    // state, not store-backed: nothing here needs a coeffect or survives
    // navigating away. The candidate list it shows DOES need one
    // (`slotPicker`, from the ViewModel) since it depends on a fresh
    // squad/lineup fetch, not just what was tapped.
    var activeSlot by remember { mutableStateOf<LineupSlot?>(null) }

    // Set the moment a fill/vacate is dispatched from the dialog, so the
    // effect below can tell "a write just finished" apart from "nothing
    // has happened yet" — `saving` alone flips false→true→false on every
    // attempt, indistinguishable from its initial idle value without this.
    // The formation dropdown doesn't need this: picking an entry closes
    // it immediately (standard dropdown behavior), there's no "Saving…"
    // state to hold it open for — see FormationDropdown.
    var awaitingSave by remember { mutableStateOf(false) }

    // Keeps the dialog open on a "Saving…" state through the write
    // (see SlotOptionsDialog) instead of closing immediately on tap, and
    // only auto-closes it once that write actually lands successfully —
    // on failure it stays open so the inline error/retry is visible
    // right where the action was taken, not just the outer saveError
    // banner underneath the dialog.
    LaunchedEffect(saving) {
        if (awaitingSave && !saving) {
            awaitingSave = false
            if (!saveError) {
                activeSlot = null
                onClosePicker()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        FormationDropdown(
            currentFormation = lineup.formation,
            saving = saving,
            onSelect = onChangeFormation,
            modifier = Modifier.fillMaxWidth()
        )

        if (saveError && activeSlot == null) {
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
                onSlotTap = { slot ->
                    activeSlot = slot
                    onRequestBenchOptions(slot)
                },
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )
        }
    }

    activeSlot?.let { slot ->
        SlotOptionsDialog(
            player = slot.player,
            picker = slotPicker,
            saving = saving,
            saveError = saveError,
            onSelect = { candidate ->
                awaitingSave = true
                onFillSlot(slot.index, candidate.id)
            },
            onVacate = {
                awaitingSave = true
                onVacate(slot.player.id)
            },
            onDismiss = {
                activeSlot = null
                onClosePicker()
            }
        )
    }
}

// One dialog for both cases a tapped slot can be in: pick a bench
// player to fill it (empty or occupied — replacing a starter is
// remove-then-fill in one motion instead of two separate taps), or, for
// an occupied slot, bench them with the "Vacate" button instead of
// picking a replacement. `player.id == 0` (see VacantPlayer) means the
// slot was already empty — nothing to vacate, so that button doesn't
// apply. Candidates split into "Specialists" (the slot's position as
// their primary) and "Jollies" (as their secondary, costs credits — see
// docs/biwenger-api-notes.md § "Starting lineup — write") the way
// Biwenger's own editor does; a jolly card is disabled, not hidden,
// when short on credits, same as an unaffordable option anywhere else
// in the app.
//
// `usePlatformDefaultWidth = false` + a fillMaxWidth modifier: the
// platform default caps a dialog around 80% of a phone's width, too
// narrow for a scrolling list of player cards to read comfortably.
@Composable
private fun SlotOptionsDialog(
    player: Player,
    picker: Loadable<BenchCandidates>?,
    saving: Boolean,
    saveError: Boolean,
    onSelect: (SquadPlayer) -> Unit,
    onVacate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val occupied = player.id != 0
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(fraction = 0.95f),
        title = { Text(if (occupied) "Replace ${player.name}" else "Fill this slot") },
        text = {
            when {
                saving -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(text = "Saving…", modifier = Modifier.padding(top = 12.dp))
                    }
                }
                saveError -> Text(
                    "Could not save your lineup right now.",
                    color = MaterialTheme.colorScheme.error
                )
                picker == null || picker is Loadable.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                picker is Loadable.Failed -> Text("Could not load your bench right now.")
                picker is Loadable.Success -> {
                    val candidates = picker.value
                    if (candidates.specialists.isEmpty() && candidates.jollies.isEmpty()) {
                        Text("No eligible players on your bench.")
                    } else {
                        Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                            if (candidates.specialists.isNotEmpty()) {
                                BenchCandidateSection(title = "Specialists") {
                                    candidates.specialists.forEach { candidate ->
                                        BenchCandidateCard(player = candidate, onClick = { onSelect(candidate) })
                                    }
                                }
                            }
                            if (candidates.jollies.isNotEmpty()) {
                                BenchCandidateSection(title = "Jollies") {
                                    candidates.jollies.forEach { candidate ->
                                        BenchCandidateCard(
                                            player = candidate,
                                            enabled = candidates.canAffordJolly,
                                            onClick = { onSelect(candidate) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (occupied) {
                TextButton(onClick = onVacate, enabled = !saving) { Text("Vacate") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } }
    )
}

// Free formations only (see FreeFormations) — offering a paid "extra"
// formation here would need the same disclose-before-spending treatment
// SlotOptionsDialog gives a jolly, and Biwenger's own picker order/set
// hasn't been reverse-engineered past this list (see
// docs/biwenger-api-notes.md § "Starting lineup — write"), so it's left
// out entirely rather than guessed at. A plain dropdown, not a modal
// dialog: picking an entry closes it immediately, same as any other
// select — no "Saving…" hold-open state to manage (contrast
// SlotOptionsDialog, which does need one; see LineupContent's
// awaitingSave). Feedback while the write is in flight is a small spinner
// swapped in for the dropdown arrow, and the control disabled — a picked
// formation doesn't show as "current" until the save actually lands, so
// something has to signal "still working" in between. A failed save
// still surfaces via the outer saveError banner underneath, same place a
// failed vacate does once its dialog has closed.
@Composable
private fun FormationDropdown(currentFormation: String, saving: Boolean, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !saving) { expanded = true },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentFormation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (saving) {
                CircularProgressIndicator(modifier = Modifier.padding(start = 4.dp).size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change formation")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FreeFormations.forEach { formation ->
                val isCurrent = formation == currentFormation
                DropdownMenuItem(
                    text = { Text(formation, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal) },
                    enabled = !isCurrent,
                    onClick = {
                        expanded = false
                        onSelect(formation)
                    }
                )
            }
        }
    }
}

@Composable
private fun BenchCandidateSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    content()
}

// Same avatar/points-badge + name + position-tag shape as
// SquadScreen.kt's SquadPlayerRow (Players subtab), minus that row's
// ownership header/footer — this card isn't about ownership, just
// "who is this and how are they doing", so ColorSurface/PlayerAvatarWithPoints/
// PositionTag are reused from ui/PlayerList.kt but the row composable
// itself isn't (see docs/coding-conventions/project-structure.md — feature-local
// rows are the norm, only the pieces shared across features get promoted).
// `enabled = false` (a jolly the manager can't afford) dims the card and
// drops its click target, rather than hiding it — seeing what's out of
// reach is more informative than a shorter list with no explanation.
@Composable
private fun BenchCandidateCard(player: SquadPlayer, onClick: () -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .alpha(if (enabled) 1f else 0.4f)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatarWithPoints(
            photoUrl = player.photoUrl,
            teamCrestUrl = player.teamCrestUrl,
            contentDescription = player.name,
            points = player.points
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                PositionTag(position = player.position, secondaryPosition = player.secondaryPosition)
            }
        }
    }
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
    onSlotTap: (LineupSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val bands = sliceLineupBands(players, parseFormation(formation))
    BoxWithConstraints(modifier = modifier) {
        val pitchSize = DpSize(maxWidth, maxHeight)
        PitchPositionRow(slots = bands.forwards, band = ForwardBand, pitchSize = pitchSize, onSlotTap = onSlotTap)
        PitchPositionRow(slots = bands.midfielders, band = MidfielderBand, pitchSize = pitchSize, onSlotTap = onSlotTap)
        PitchPositionRow(slots = bands.defenders, band = DefenderBand, pitchSize = pitchSize, onSlotTap = onSlotTap)
        PitchPositionRow(
            slots = bands.goalkeepers,
            band = PositionBand(GoalkeeperRow, GoalkeeperRow),
            pitchSize = pitchSize,
            onSlotTap = onSlotTap
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

// One rendered pitch slot: `index` into the lineup's raw playersID-order
// array (what a fill/vacate write needs to target this exact slot —
// there's no player id to key off when `player` is the vacant
// placeholder), `position` is the band's catalogue position code
// (1=GK 2=DF 3=MF 4=FW, see docs/biwenger-api-notes.md § "Position
// codes") used to filter bench-picker eligibility, `player` is what
// PitchPlayer renders.
data class LineupSlot(val index: Int, val position: Int, val player: Player)

data class LineupBands(
    val goalkeepers: List<LineupSlot>,
    val defenders: List<LineupSlot>,
    val midfielders: List<LineupSlot>,
    val forwards: List<LineupSlot>,
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
// the placeholder PitchPlayer renders. Each slot keeps its index into
// `players` (not just its position within the band) — a fill/vacate
// write needs to target that exact array slot.
fun sliceLineupBands(players: List<Player?>, counts: FormationCounts): LineupBands {
    var remaining = players
    var offset = 0
    fun take(expectedCount: Int, position: Int): List<LineupSlot> {
        val slice = remaining.take(expectedCount)
        val startIndex = offset
        remaining = remaining.drop(slice.size)
        offset += slice.size
        return withVacantSlots(slice, expectedCount).mapIndexed { i, player ->
            LineupSlot(index = startIndex + i, position = position, player = player)
        }
    }
    return LineupBands(
        goalkeepers = take(1, position = 1),
        defenders = take(counts.defenders, position = 2),
        midfielders = take(counts.midfielders, position = 3),
        forwards = take(counts.forwards, position = 4),
    )
}

// Reshapes the current eleven for a new formation, for
// change-lineup-formation: switching formation changes every outfield
// band's size at once (e.g. 3-5-2 -> 4-4-2 is DF 3->4, MF 5->4), so the
// current eleven can't just carry over as-is, and the write endpoint
// needs a full array sized to the NEW formation regardless (see
// docs/biwenger-api-notes.md § "Starting lineup — write"). Best-effort
// per band, reusing the existing slicing: keep up to the new count of
// currently-fielded players in that band, in their existing order —
// anyone beyond the new count is dropped (benched, not deleted — still
// on the squad) and a bigger new count pads with vacant (`null`) slots,
// fillable via the already-shipped slot-fill flow. The goalkeeper band
// is always exactly 1 in any formation, so it always carries over
// untouched. Returns ids only (`playersID`'s shape), not `LineupSlot`s —
// this is building a write request, not something rendered.
fun reshapeLineup(players: List<Player?>, currentFormation: String, newFormation: String): List<Int?> {
    val oldBands = sliceLineupBands(players, parseFormation(currentFormation))
    val newCounts = parseFormation(newFormation)
    fun carryOver(slots: List<LineupSlot>, newCount: Int): List<Int?> {
        val ids = slots.take(newCount).map { slot -> slot.player.id.takeIf { it != 0 } }
        return ids + List((newCount - ids.size).coerceAtLeast(0)) { null }
    }
    return carryOver(oldBands.goalkeepers, 1) +
        carryOver(oldBands.defenders, newCounts.defenders) +
        carryOver(oldBands.midfielders, newCounts.midfielders) +
        carryOver(oldBands.forwards, newCounts.forwards)
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
private fun PitchPositionRow(slots: List<LineupSlot>, band: PositionBand, pitchSize: DpSize, onSlotTap: (LineupSlot) -> Unit) {
    val count = slots.size
    val midpointRow = (band.highestRow + band.lowestRow) / 2f
    val halfSpan = if (count == 2) PairHalfSpanColumns else MaxHalfSpanColumns.toFloat()

    slots.forEachIndexed { index, slot ->
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
            slot = slot,
            onTap = onSlotTap,
            modifier = Modifier
                .width(PitchPlayerWidth)
                .offset(x = centerX - PitchPlayerWidth / 2, y = bottomY - PitchPlayerHeight)
        )
    }
}

private val PitchPlayerWidth = 88.dp
private val PitchPlayerHeight = 66.dp

private val PitchPlayerPhotoSize = 48.dp

// Every slot is tappable now — a vacant one (id 0, see VacantPlayer
// above) opens the bench picker instead of the vacate dialog;
// LineupContent's onSlotTap tells the two apart.
@Composable
private fun PitchPlayer(slot: LineupSlot, onTap: (LineupSlot) -> Unit, modifier: Modifier = Modifier) {
    val player = slot.player
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable { onTap(slot) }) {
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
