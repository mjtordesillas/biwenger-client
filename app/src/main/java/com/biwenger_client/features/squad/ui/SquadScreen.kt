@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biwenger_client.features.squad.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.features.squad.domain.models.Player
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.domain.models.PricePoint
import com.biwenger_client.ui.theme.ColorDivider
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral500
import com.biwenger_client.ui.theme.Neutral900
import com.biwenger_client.ui.theme.NocturneRadius
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val PositionLabels = mapOf(1 to "GK", 2 to "DF", 3 to "MF", 4 to "FW")
private val FilterPositions = listOf(null, 1, 2, 3, 4) // null = All

@Composable
fun SquadScreen(
    viewModel: SquadViewModel = hiltViewModel()
) {
    val players by viewModel.players
    val selectedPosition by viewModel.selectedPosition
    val selectedPlayerId by viewModel.selectedPlayerId
    val priceHistory by viewModel.priceHistory

    SquadScreen(
        players = players,
        selectedPosition = selectedPosition,
        selectedPlayerId = selectedPlayerId,
        priceHistory = priceHistory,
        onPositionSelected = viewModel::positionFilterChanged,
        onPlayerTapped = viewModel::playerTapped,
        onSheetDismissed = viewModel::sheetClosed,
    )
}

@Composable
private fun SquadScreen(
    players: Loadable<List<Player>>,
    selectedPosition: Int?,
    selectedPlayerId: Int?,
    priceHistory: Loadable<PriceHistory>?,
    onPositionSelected: (Int?) -> Unit,
    onPlayerTapped: (Int) -> Unit,
    onSheetDismissed: () -> Unit,
) {
    val allPlayers = (players as? Loadable.Success)?.value.orEmpty()
    val filteredPlayers = allPlayers.filter { selectedPosition == null || it.position == selectedPosition }
    val selectedPlayer = allPlayers.find { it.id == selectedPlayerId }

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

    if (selectedPlayer != null) {
        PlayerDetailSheet(player = selectedPlayer, priceHistory = priceHistory, onDismissed = onSheetDismissed)
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
private fun FilterChip(label: String, color: Color, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(NocturneRadius.md))
            .border(1.dp, if (active) color else ColorDivider, RoundedCornerShape(NocturneRadius.md))
            .background(if (active) color.copy(alpha = 0.22f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = if (active) color else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PlayerList(players: List<Player>, onPlayerTapped: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(players) { player ->
            PlayerRow(player = player, onClick = { onPlayerTapped(player.id) })
        }
    }
}

@Composable
private fun PlayerRow(player: Player, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatar(player = player, size = 48.dp)

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                PositionTag(player = player)
                Text(
                    text = "  ${player.points} pts",
                    style = MaterialTheme.typography.labelMedium,
                    color = Neutral500
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatPrice(player.price), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
            PriceTrend(priceIncrement = player.priceIncrement)
        }
    }
}

@Composable
private fun PlayerAvatar(player: Player, size: Dp) {
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Neutral900)
        ) {
            AsyncImage(
                model = player.photoUrl,
                contentDescription = player.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // No shape/background here on purpose — a team crest is its own
        // shape (shield, circle, whatever the club uses), not forced into
        // one mask. ContentScale.Fit shows the real image untouched.
        AsyncImage(
            model = player.teamCrestUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.4f)
                .align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun PositionTag(player: Player) {
    val color = PositionColors[player.position] ?: Neutral500
    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(NocturneRadius.md * 0.75f))
                .background(color.copy(alpha = 0.24f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = PositionLabels[player.position] ?: player.position.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        val secondaryColor = player.secondaryPosition?.let { PositionColors[it] }
        if (secondaryColor != null) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .align(Alignment.BottomEnd)
                    .border(1.5.dp, ColorSurface, CircleShape)
                    .background(secondaryColor, CircleShape)
            )
        }
    }
}

@Composable
private fun PriceTrend(priceIncrement: Long) {
    val (icon, color) = priceTrend(priceIncrement)
    Text(
        text = "$icon ${formatPriceChange(priceIncrement)}",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

// Shared with the price history chart below — one trend color per player,
// not recomputed per place it's shown.
private fun priceTrend(priceIncrement: Long): Pair<String, Color> = when {
    priceIncrement > 0 -> "↑" to TrendUp
    priceIncrement < 0 -> "↓" to TrendDown
    else -> "–" to TrendFlat
}

@Composable
private fun PlayerDetailSheet(player: Player, priceHistory: Loadable<PriceHistory>?, onDismissed: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissed,
        sheetState = sheetState,
        containerColor = ColorSurface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(player = player, size = 68.dp)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.padding(top = 6.dp)) { PositionTag(player = player) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailStat(
                    label = "Price",
                    value = formatPrice(player.price),
                    modifier = Modifier.weight(1f),
                    subtitle = { PriceTrend(priceIncrement = player.priceIncrement) }
                )
                DetailStat(label = "Points", value = "${player.points}", modifier = Modifier.weight(1f))
            }

            PriceHistorySection(
                priceHistory = priceHistory,
                trendColor = priceTrend(player.priceIncrement).second
            )
        }
    }
}

@Composable
private fun DetailStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: @Composable () -> Unit = { Text(text = " ", fontSize = 11.5.sp) },
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp))
        Box(modifier = Modifier.padding(top = 1.dp)) { subtitle() }
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 0.6.sp,
            color = Neutral500,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun formatPrice(price: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(price)

private fun formatPriceChange(priceIncrement: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(abs(priceIncrement))

private enum class PriceHistoryTab { LAST_YEAR, CURRENT_SEASON }

@Composable
private fun PriceHistorySection(priceHistory: Loadable<PriceHistory>?, trendColor: Color) {
    var tab by remember { mutableStateOf(PriceHistoryTab.CURRENT_SEASON) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            PriceHistoryTabButton(
                label = "Current season",
                selected = tab == PriceHistoryTab.CURRENT_SEASON,
                onClick = { tab = PriceHistoryTab.CURRENT_SEASON },
                modifier = Modifier.weight(1f)
            )
            PriceHistoryTabButton(
                label = "Last Year",
                selected = tab == PriceHistoryTab.LAST_YEAR,
                onClick = { tab = PriceHistoryTab.LAST_YEAR },
                modifier = Modifier.weight(1f)
            )
        }

        when (priceHistory) {
            null, is Loadable.Loading -> Box(
                modifier = Modifier.fillMaxWidth().height(62.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            }
            is Loadable.Failed -> Text(
                text = "Could not load price history right now.",
                fontSize = 12.sp,
                color = Neutral500,
                modifier = Modifier.padding(vertical = 20.dp)
            )
            is Loadable.Success -> {
                val history = priceHistory.value
                val points = when (tab) {
                    PriceHistoryTab.LAST_YEAR -> history.prices
                    PriceHistoryTab.CURRENT_SEASON -> history.prices.filter { it.date >= history.seasonStart }
                }
                PriceHistoryChart(points = points, trendColor = trendColor)
            }
        }
    }
}

@Composable
private fun PriceHistoryTabButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = if (selected) MaterialTheme.colorScheme.onSurface else Neutral500
    val underline = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                drawLine(
                    color = underline,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(bottom = 9.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PriceHistoryChart(points: List<PricePoint>, trendColor: Color) {
    if (points.size < 2) {
        Text(
            text = "Not enough data yet.",
            fontSize = 12.sp,
            color = Neutral500,
            modifier = Modifier.padding(vertical = 20.dp)
        )
        return
    }

    val minPrice = points.minOf { it.price }
    val maxPrice = points.maxOf { it.price }
    val range = (maxPrice - minPrice).coerceAtLeast(1)

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f).height(62.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepX = size.width / (points.size - 1)
                fun yFor(price: Long) = size.height - ((price - minPrice).toFloat() / range) * size.height

                val linePath = Path()
                points.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = yFor(point.price)
                    if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                }
                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(trendColor.copy(alpha = 0.35f), trendColor.copy(alpha = 0f))
                    )
                )
                drawPath(
                    path = linePath,
                    color = trendColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        Column(modifier = Modifier.width(46.dp).height(62.dp)) {
            Text(text = formatPriceCompact(maxPrice), fontSize = 9.sp, color = Neutral500)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = formatPriceCompact(minPrice), fontSize = 9.sp, color = Neutral500)
        }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, end = 46.dp)) {
        xAxisLabelIndices(count = points.size).forEach { index ->
            Text(
                text = formatAxisDate(points[index].date),
                fontSize = 9.sp,
                color = Neutral500,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Evenly spaced label positions across the series — real daily data has
// no natural "6m/3m/1m" ticks to hang labels on the way the design mock's
// placeholder data did, so this samples the date axis instead.
private fun xAxisLabelIndices(count: Int, labelCount: Int = 4): List<Int> {
    if (count <= labelCount) return (0 until count).toList()
    return (0 until labelCount).map { i -> (i * (count - 1)) / (labelCount - 1) }
}

private val MonthAbbreviations = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

// Manual parsing, not java.time — the date is always our own ISO
// yyyy-MM-dd (biwenger-client's price-history-view.js), no locale/parsing
// edge cases to hand off to a date library for.
private fun formatAxisDate(isoDate: String): String {
    val parts = isoDate.split("-")
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return isoDate
    val day = parts.getOrNull(2)?.toIntOrNull() ?: return isoDate
    return "$day ${MonthAbbreviations.getOrElse(month - 1) { "" }}"
}

private fun formatPriceCompact(price: Long): String =
    String.format(Locale.US, "€%.1fm", price / 1_000_000.0)
