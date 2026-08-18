package com.biwenger_client.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

// A football pitch's markings — outer boundary, halfway line, center
// circle, both penalty areas — drawn to fill whatever size/aspect ratio
// the caller gives it. No Material icon matches "pitch outline", and
// this same drawing scales down cleanly for a small tab icon and up for
// the full Lineup background, so it's one composable rather than a
// vector asset per size. `fillColor` is optional — omitted, it's just
// the line-art outline (the tab icon); set, it's the pitch turf (the
// Lineup screen's background).
@Composable
fun FootballPitch(modifier: Modifier = Modifier, lineColor: Color = Color.White, fillColor: Color? = null) {
    Canvas(modifier = modifier) {
        fillColor?.let { drawRect(color = it) }

        val strokeWidth = size.minDimension * 0.025f
        val stroke = Stroke(width = strokeWidth)
        val inset = strokeWidth / 2

        drawRect(
            color = lineColor,
            topLeft = Offset(inset, inset),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = stroke
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth
        )
        drawCircle(
            color = lineColor,
            radius = size.minDimension * 0.16f,
            center = Offset(size.width / 2, size.height / 2),
            style = stroke
        )

        val boxWidth = size.width * 0.6f
        val boxHeight = size.height * 0.14f
        val boxLeft = (size.width - boxWidth) / 2
        val centerX = size.width / 2
        drawRect(
            color = lineColor,
            topLeft = Offset(boxLeft, inset),
            size = Size(boxWidth, boxHeight),
            style = stroke
        )
        drawRect(
            color = lineColor,
            topLeft = Offset(boxLeft, size.height - boxHeight - inset),
            size = Size(boxWidth, boxHeight),
            style = stroke
        )

        // The "D" — a half-circle sitting on each box's outer edge,
        // bulging away from the goal — plus the penalty spot inside it.
        val arcRadius = size.minDimension * 0.13f
        val arcBox = Size(arcRadius * 2, arcRadius * 2)
        drawArc(
            color = lineColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - arcRadius, boxHeight - arcRadius),
            size = arcBox,
            style = stroke
        )
        drawArc(
            color = lineColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - arcRadius, size.height - boxHeight - arcRadius),
            size = arcBox,
            style = stroke
        )

        val spotRadius = strokeWidth * 1.3f
        drawCircle(color = lineColor, radius = spotRadius, center = Offset(centerX, boxHeight * 0.72f))
        drawCircle(color = lineColor, radius = spotRadius, center = Offset(centerX, size.height - boxHeight * 0.72f))
    }
}
