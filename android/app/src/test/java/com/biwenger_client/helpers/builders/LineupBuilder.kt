package com.biwenger_client.helpers.builders

import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.lineup.domain.models.Lineup

fun aLineup(
    formation: String = "3-5-2",
    players: List<Player?> = listOf(aPlayer()),
    credits: Int = 20,
) = Lineup(
    formation = formation,
    players = players,
    credits = credits,
)
