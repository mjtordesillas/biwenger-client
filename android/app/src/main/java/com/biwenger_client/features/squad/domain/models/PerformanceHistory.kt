package com.biwenger_client.features.squad.domain.models

data class GameweekPoints(
    val matchDay: Int,
    val points: Int?
)

data class PerformanceHistory(
    val gameweeks: List<GameweekPoints>
)
