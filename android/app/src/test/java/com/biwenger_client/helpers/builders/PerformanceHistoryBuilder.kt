package com.biwenger_client.helpers.builders

import com.biwenger_client.features.squad.domain.models.GameweekPoints
import com.biwenger_client.features.squad.domain.models.PerformanceHistory

fun aGameweekPoints(
    matchDay: Int = 1,
    points: Int? = 4,
) = GameweekPoints(matchDay = matchDay, points = points)

fun aPerformanceHistory(
    gameweeks: List<GameweekPoints> = listOf(aGameweekPoints()),
) = PerformanceHistory(gameweeks = gameweeks)
