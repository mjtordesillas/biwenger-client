package com.biwenger_client.helpers.builders

import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.MatchDayTeam
import com.biwenger_client.features.squad.domain.models.ScoreBreakdown
import com.biwenger_client.features.squad.domain.models.ScoreRow
import com.biwenger_client.features.squad.domain.models.SubstitutionEvent

fun aMatchDayTeam(
    id: Int = 87,
    name: String = "Betis",
    score: Int = 2,
    crestUrl: String = "https://cdn.biwenger.com/i/t/87.png",
) = MatchDayTeam(id = id, name = name, score = score, crestUrl = crestUrl)

fun aScoreRow(
    type: String = "picas",
    count: Int? = 2,
    rating: Double? = null,
    points: Int? = 6,
) = ScoreRow(type = type, count = count, rating = rating, points = points)

fun aScoreBreakdown(
    points: Int? = 6,
    rows: List<ScoreRow> = listOf(aScoreRow()),
) = ScoreBreakdown(points = points, rows = rows)

fun aSubstitutionEvent(
    type: String = "substitutedOff",
    minute: Int = 70,
) = SubstitutionEvent(type = type, minute = minute)

fun aMatchDayDetails(
    matchDay: Int = 8,
    kickoff: Long = 1741604400,
    home: MatchDayTeam = aMatchDayTeam(),
    away: MatchDayTeam = aMatchDayTeam(id = 91, name = "Alavés", score = 1, crestUrl = "https://cdn.biwenger.com/i/t/91.png"),
    diarioAs: ScoreBreakdown = aScoreBreakdown(),
    sofaScore: ScoreBreakdown = aScoreBreakdown(points = 4, rows = listOf(aScoreRow(type = "sofascore", count = null, rating = 6.4, points = 4))),
    media: Int? = 5,
    substitutions: List<SubstitutionEvent> = emptyList(),
) = MatchDayDetails(
    matchDay = matchDay,
    kickoff = kickoff,
    home = home,
    away = away,
    diarioAs = diarioAs,
    sofaScore = sofaScore,
    media = media,
    substitutions = substitutions,
)
