package com.biwenger_client.features.squad.domain.models

import com.biwenger_client.helpers.builders.aSquadPlayer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SquadPlayerTest {

    @Test
    fun `positionSortRank orders goalkeepers, then each outfield position banded by secondary role`() {
        val goalkeeper = aSquadPlayer(position = 1, secondaryPosition = null)
        val defender = aSquadPlayer(position = 2, secondaryPosition = null)
        val defenderMidfielder = aSquadPlayer(position = 2, secondaryPosition = 3)
        val midfielderDefender = aSquadPlayer(position = 3, secondaryPosition = 2)
        val midfielder = aSquadPlayer(position = 3, secondaryPosition = null)
        val midfielderForward = aSquadPlayer(position = 3, secondaryPosition = 4)
        val forwardMidfielder = aSquadPlayer(position = 4, secondaryPosition = 3)
        val forward = aSquadPlayer(position = 4, secondaryPosition = null)

        val ranks = listOf(
            goalkeeper, defender, defenderMidfielder, midfielderDefender,
            midfielder, midfielderForward, forwardMidfielder, forward
        ).map { it.positionSortRank }

        assertThat(ranks).isSorted()
        assertThat(ranks).doesNotHaveDuplicates()
    }

    @Test
    fun `positionSortRank sorts an uncovered combination last`() {
        val defenderForward = aSquadPlayer(position = 2, secondaryPosition = 4)
        val forward = aSquadPlayer(position = 4, secondaryPosition = null)

        assertThat(defenderForward.positionSortRank).isGreaterThan(forward.positionSortRank)
    }
}
