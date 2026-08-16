package com.biwenger_client.features.squad.domain.models

data class MatchDayTeam(
    val id: Int,
    val name: String,
    val score: Int,
    val crestUrl: String,
)

data class MatchDayDetails(
    val matchDay: Int,
    val kickoff: Long,
    val home: MatchDayTeam,
    val away: MatchDayTeam,
)
