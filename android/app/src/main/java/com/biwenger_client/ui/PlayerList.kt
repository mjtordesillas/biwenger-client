package com.biwenger_client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.biwenger_client.domain.models.Player
import com.biwenger_client.ui.theme.ColorDivider
import com.biwenger_client.ui.theme.ColorSurface
import com.biwenger_client.ui.theme.Neutral500
import com.biwenger_client.ui.theme.Neutral900
import com.biwenger_client.ui.theme.NocturneRadius
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// Promoted out of features/squad/ui/SquadScreen.kt — market is a second
// consumer of the same player-row rendering (same Player shape, same
// design). See docs/coding-conventions/project-structure.md.

val PositionLabels = mapOf(1 to "GK", 2 to "DF", 3 to "MF", 4 to "FW")

@Composable
fun PlayerList(players: List<Player>, onPlayerTapped: (Int) -> Unit) {
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
fun PlayerRow(player: Player, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NocturneRadius.md))
            .background(ColorSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatar(photoUrl = player.photoUrl, teamCrestUrl = player.teamCrestUrl, contentDescription = player.name, size = 48.dp)

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                PositionTag(position = player.position, secondaryPosition = player.secondaryPosition)
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
fun PlayerAvatar(
    photoUrl: String,
    teamCrestUrl: String,
    contentDescription: String,
    size: Dp,
    crestSize: Dp = size * 0.4f,
    // Lets a caller that overlays something else (e.g. Market's points
    // badge) at the bottom of the avatar push the crest down/outward to
    // match — 0dp defaults keep every other call site flush with the
    // avatar edge.
    crestOffsetX: Dp = 0.dp,
    crestOffsetY: Dp = 0.dp,
) {
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Neutral900)
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // No shape/background here on purpose — a team crest is its own
        // shape (shield, circle, whatever the club uses), not forced into
        // one mask. ContentScale.Fit shows the real image untouched.
        AsyncImage(
            model = teamCrestUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(crestSize)
                .align(Alignment.BottomStart)
                .offset(x = crestOffsetX, y = crestOffsetY)
        )
    }
}

@Composable
fun PositionTag(position: Int, secondaryPosition: Int?) {
    val color = PositionColors[position] ?: Neutral500
    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(NocturneRadius.md * 0.75f))
                .background(color.copy(alpha = 0.24f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = PositionLabels[position] ?: position.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        val secondaryColor = secondaryPosition?.let { PositionColors[it] }
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
fun PriceTrend(priceIncrement: Long) {
    val (icon, color) = priceTrend(priceIncrement)
    Text(
        text = "$icon ${formatPriceChange(priceIncrement)}",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

fun priceTrend(priceIncrement: Long) = when {
    priceIncrement > 0 -> "↑" to TrendUp
    priceIncrement < 0 -> "↓" to TrendDown
    else -> "–" to TrendFlat
}

fun formatPrice(price: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(price)

fun formatPriceChange(priceIncrement: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(abs(priceIncrement))

// Promoted out of features/squad/ui/SquadScreen.kt alongside
// PlayerDetailScreen.kt — used by both squad's position filter row and
// the shared detail screen's season filter.
@Composable
fun FilterChip(label: String, color: Color, active: Boolean, onClick: () -> Unit) {
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
