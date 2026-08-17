package com.biwenger_client.features.squad.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
import com.biwenger_client.features.squad.domain.models.GameweekPoints
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.MatchDayTeam
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.Player
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.domain.models.PricePoint
import com.biwenger_client.features.squad.domain.models.ScoreBreakdown
import com.biwenger_client.features.squad.domain.models.ScoreRow
import com.biwenger_client.features.squad.domain.models.SubstitutionEvent
import com.biwenger_client.ui.theme.ColorDivider
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral500
import com.biwenger_client.ui.theme.Neutral700
import com.biwenger_client.ui.theme.Neutral800
import com.biwenger_client.ui.theme.Neutral900
import com.biwenger_client.ui.theme.NocturneRadius
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val PositionLabels = mapOf(1 to "GK", 2 to "DF", 3 to "MF", 4 to "FW")
private val FilterPositions = listOf(null, 1, 2, 3, 4) // null = All
private val PriceHistoryCardHeight = 158.dp

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

private fun priceTrend(priceIncrement: Long): Pair<String, Color> = when {
    priceIncrement > 0 -> "↑" to TrendUp
    priceIncrement < 0 -> "↓" to TrendDown
    else -> "–" to TrendFlat
}

@Composable
private fun PlayerDetailScreen(
    player: Player,
    priceHistory: Loadable<PriceHistory>?,
    performanceHistory: Loadable<PerformanceHistory>?,
    performanceHistorySeason: String,
    onPerformanceSeasonChanged: (String) -> Unit,
    onMatchDayTapped: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                fontSize = 22.sp,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 10.dp)
            )
            Text(text = player.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }

        Column(modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(player = player, size = 68.dp)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    PositionTag(player = player)
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

            PerformanceHistorySection(
                performanceHistory = performanceHistory,
                season = performanceHistorySeason,
                onSeasonChanged = onPerformanceSeasonChanged,
                onMatchDayTapped = onMatchDayTapped
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
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(NocturneRadius.md))
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
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
    if (priceHistory == null || priceHistory is Loadable.Loading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(PriceHistoryCardHeight)
                .clip(RoundedCornerShape(NocturneRadius.md))
                .background(rememberShimmerBrush())
        )
        return
    }

    var tab by remember { mutableStateOf(PriceHistoryTab.CURRENT_SEASON) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(PriceHistoryCardHeight)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(NocturneRadius.md))
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
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
            is Loadable.Loading -> Unit
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
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "priceHistorySkeleton")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(Neutral800, Neutral700, Neutral800),
        start = Offset(translate - 200f, 0f),
        end = Offset(translate, 0f)
    )
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

private fun xAxisLabelIndices(count: Int, labelCount: Int = 4): List<Int> {
    if (count <= labelCount) return (0 until count).toList()
    return (0 until labelCount).map { i -> (i * (count - 1)) / (labelCount - 1) }
}

private val MonthAbbreviations = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun formatAxisDate(isoDate: String): String {
    val parts = isoDate.split("-")
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return isoDate
    val day = parts.getOrNull(2)?.toIntOrNull() ?: return isoDate
    return "$day ${MonthAbbreviations.getOrElse(month - 1) { "" }}"
}

private fun formatPriceCompact(price: Long): String =
    String.format(Locale.US, "€%.1fm", price / 1_000_000.0)

private val PerformanceBarAreaHeight = 84.dp

@Composable
private fun PerformanceHistorySection(
    performanceHistory: Loadable<PerformanceHistory>?,
    season: String,
    onSeasonChanged: (String) -> Unit,
    onMatchDayTapped: (Int) -> Unit,
) {
    if (performanceHistory == null || performanceHistory is Loadable.Loading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(PriceHistoryCardHeight)
                .clip(RoundedCornerShape(NocturneRadius.md))
                .background(rememberShimmerBrush())
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(NocturneRadius.md))
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Player performance",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    label = "Current",
                    color = MaterialTheme.colorScheme.primary,
                    active = season == SquadViewModel.CURRENT_SEASON,
                    onClick = { onSeasonChanged(SquadViewModel.CURRENT_SEASON) }
                )
                FilterChip(
                    label = "Previous",
                    color = MaterialTheme.colorScheme.primary,
                    active = season == SquadViewModel.PREVIOUS_SEASON,
                    onClick = { onSeasonChanged(SquadViewModel.PREVIOUS_SEASON) }
                )
            }
        }

        when (performanceHistory) {
            is Loadable.Failed -> Text(
                text = "Could not load performance history right now.",
                fontSize = 12.sp,
                color = Neutral500,
                modifier = Modifier.padding(vertical = 20.dp)
            )
            is Loadable.Success -> {
                val gameweeks = performanceHistory.value.gameweeks
                if (gameweeks.isEmpty()) {
                    val emptyMessage = if (season == SquadViewModel.PREVIOUS_SEASON) {
                        "No matches played last season."
                    } else {
                        "No matches played yet this season."
                    }
                    Text(
                        text = emptyMessage,
                        fontSize = 12.sp,
                        color = Neutral500,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    PerformanceChart(gameweeks = gameweeks, onMatchDayTapped = onMatchDayTapped)
                }
            }
            is Loadable.Loading -> Unit
        }
    }
}

@Composable
private fun PerformanceChart(gameweeks: List<GameweekPoints>, onMatchDayTapped: (Int) -> Unit) {
    val maxPoints = (gameweeks.maxOfOrNull { it.points ?: 0 } ?: 0).coerceAtLeast(0)
    val minPoints = (gameweeks.minOfOrNull { it.points ?: 0 } ?: 0).coerceAtMost(0)
    val range = (maxPoints - minPoints).coerceAtLeast(1)

    Row(modifier = Modifier.fillMaxWidth()) {
        PerformanceYAxis(maxPoints = maxPoints, minPoints = minPoints)

        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gameweeks.forEach { gameweek ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(28.dp).clickable { onMatchDayTapped(gameweek.matchDay) }
                ) {
                    Box(modifier = Modifier.height(PerformanceBarAreaHeight).fillMaxWidth()) {
                        val points = gameweek.points ?: 0
                        val zeroY = PerformanceBarAreaHeight * (maxPoints.toFloat() / range)
                        val barHeight = (PerformanceBarAreaHeight * (abs(points).toFloat() / range)).coerceAtLeast(2.dp)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = if (points >= 0) zeroY - barHeight else zeroY)
                                .width(18.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(performanceBarColor(gameweek.points))
                        )
                    }
                    Text(
                        text = "${gameweek.matchDay}",
                        fontSize = 10.sp,
                        color = Neutral500,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceYAxis(maxPoints: Int, minPoints: Int) {
    val midPoints = (maxPoints + minPoints) / 2
    Column(
        modifier = Modifier
            .width(22.dp)
            .height(PerformanceBarAreaHeight)
            .background(ColorSurface)
            .padding(end = 4.dp)
    ) {
        Text(text = "$maxPoints", fontSize = 9.sp, color = Neutral500)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "$midPoints", fontSize = 9.sp, color = Neutral500)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "$minPoints", fontSize = 9.sp, color = Neutral500)
    }
}

private fun performanceBarColor(points: Int?): Color = when {
    points == null -> Neutral500
    points < 2 -> PerformanceLow
    points < 6 -> PerformanceMid
    points < 10 -> PerformanceHigh
    else -> PerformanceGreat
}

@Composable
private fun MatchDayDetailsScreen(player: Player, matchDayDetails: Loadable<MatchDayDetails>?, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                fontSize = 22.sp,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 10.dp)
            )
            Text(text = matchDayTitle(matchDayDetails), style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }

        when (matchDayDetails) {
            null, is Loadable.Loading -> Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is Loadable.Failed -> Text(
                text = "Could not load match day details right now.",
                modifier = Modifier.padding(20.dp)
            )
            is Loadable.Success -> MatchDayContent(player = player, details = matchDayDetails.value)
        }
    }
}

private fun matchDayTitle(matchDayDetails: Loadable<MatchDayDetails>?): String =
    if (matchDayDetails is Loadable.Success) {
        "Match day ${matchDayDetails.value.matchDay} | ${formatKickoff(matchDayDetails.value.kickoff)}"
    } else {
        "Match day"
    }

@Composable
private fun MatchDayContent(player: Player, details: MatchDayDetails) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        MatchDayPlayerSummary(player = player, points = details.media)
        MatchDayHeader(details = details)
        if (details.substitutions.isNotEmpty()) {
            SubstitutionsSection(substitutions = details.substitutions)
        }
        ScoreBreakdownSection(title = "Diario AS", breakdown = details.diarioAs)
        ScoreBreakdownSection(title = "SofaScore", breakdown = details.sofaScore)
        MediaSection(diarioAs = details.diarioAs.points, sofaScore = details.sofaScore.points, media = details.media)
    }
}

@Composable
private fun MatchDayPlayerSummary(player: Player, points: Int?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
    ) {
        PlayerAvatar(player = player, size = 68.dp)
        Text(text = player.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Text(
            text = "${points ?: "–"} points",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = performanceBarColor(points)
        )
    }
}

@Composable
private fun MatchDayHeader(details: MatchDayDetails) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchDayTeamColumn(team = details.home)
        Text(
            text = "${details.home.score} - ${details.away.score}",
            style = MaterialTheme.typography.titleLarge
        )
        MatchDayTeamColumn(team = details.away)
    }
}

@Composable
private fun MatchDayTeamColumn(team: MatchDayTeam) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = team.crestUrl,
            contentDescription = team.name,
            modifier = Modifier.size(40.dp)
        )
        Text(text = team.name, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

private fun formatKickoff(kickoff: Long): String {
    val formatter = SimpleDateFormat("MMM d (EEE) - HH:mm", Locale.getDefault())
    return formatter.format(Date(kickoff * 1000))
}

@Composable
private fun SubstitutionsSection(substitutions: List<SubstitutionEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .padding(14.dp)
    ) {
        substitutions.forEach { substitution ->
            DetailRow(label = substitutionLabel(substitution), value = "${substitution.minute}'")
        }
    }
}

private fun substitutionLabel(substitution: SubstitutionEvent): String = when (substitution.type) {
    "substitutedOn" -> "↑ Substituted on"
    "substitutedOff" -> "↓ Substituted off"
    else -> substitution.type
}

@Composable
private fun ScoreBreakdownSection(title: String, breakdown: ScoreBreakdown) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .padding(14.dp)
    ) {
        DetailRow(label = title, value = "${breakdown.points ?: "–"} Points", emphasized = true)
        breakdown.rows.forEach { row ->
            DetailRow(label = scoreRowLabel(row), value = signed(row.points))
        }
    }
}

@Composable
private fun MediaSection(diarioAs: Int?, sofaScore: Int?, media: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .padding(14.dp)
    ) {
        DetailRow(
            label = "Average: (${diarioAs ?: "–"} + ${sofaScore ?: "–"}) / 2",
            value = "${media ?: "–"} points",
            emphasized = true
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, emphasized: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = if (emphasized) 13.sp else 12.sp, fontWeight = if (emphasized) FontWeight.SemiBold else null)
        Text(text = value, fontSize = if (emphasized) 13.sp else 12.sp, fontWeight = if (emphasized) FontWeight.SemiBold else null)
    }
}

private fun signed(points: Int?): String = when {
    points == null -> "–"
    points >= 0 -> "+$points"
    else -> "$points"
}

private fun scoreRowLabel(row: ScoreRow): String = when (row.type) {
    "picas" -> "${row.count} Spades ${"♠".repeat(row.count ?: 0)}"
    "sofascore" -> "${row.rating} SofaScore"
    "goal" -> if (row.count == 1) "1 Goal ⚽" else "${row.count} Goals ⚽"
    "penalty" -> if (row.count == 1) "1 Penalty goal ⚽" else "${row.count} Penalty goals ⚽"
    "assist" -> if (row.count == 1) "1 Assist" else "${row.count} Assists"
    "redCard" -> "Red card"
    "secondYellowCard" -> "Second yellow"
    else -> row.type
}
