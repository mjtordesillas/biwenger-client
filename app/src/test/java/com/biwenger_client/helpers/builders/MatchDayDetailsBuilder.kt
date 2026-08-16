package com.biwenger_client.helpers.builders

import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.MatchDayTeam

fun aMatchDayTeam(
    id: Int = 87,
    name: String = "Betis",
    score: Int = 2,
    crestUrl: String = "https://cdn.biwenger.com/i/t/87.png",
) = MatchDayTeam(id = id, name = name, score = score, crestUrl = crestUrl)

fun aMatchDayDetails(
    matchDay: Int = 8,
    kickoff: Long = 1741604400,
    home: MatchDayTeam = aMatchDayTeam(),
    away: MatchDayTeam = aMatchDayTeam(id = 91, name = "Alavés", score = 1, crestUrl = "https://cdn.biwenger.com/i/t/91.png"),
) = MatchDayDetails(matchDay = matchDay, kickoff = kickoff, home = home, away = away)
