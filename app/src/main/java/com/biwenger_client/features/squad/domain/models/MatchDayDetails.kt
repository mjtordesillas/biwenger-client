package com.biwenger_client.features.squad.domain.models

import com.google.gson.annotations.SerializedName

data class MatchDayTeam(
    val id: Int,
    val name: String,
    val score: Int,
    val crestUrl: String,
)

data class ScoreRow(
    val type: String,
    val count: Int? = null,
    val rating: Double? = null,
    val points: Int? = null,
)

data class ScoreBreakdown(
    val points: Int?,
    val rows: List<ScoreRow>,
)

data class SubstitutionEvent(
    val type: String,
    val minute: Int,
)

data class MatchDayDetails(
    val matchDay: Int,
    val kickoff: Long,
    val home: MatchDayTeam,
    val away: MatchDayTeam,
    @SerializedName("as") val diarioAs: ScoreBreakdown,
    val sofaScore: ScoreBreakdown,
    val media: Int?,
    val substitutions: List<SubstitutionEvent>,
)
