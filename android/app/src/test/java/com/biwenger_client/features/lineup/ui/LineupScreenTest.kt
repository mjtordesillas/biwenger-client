package com.biwenger_client.features.lineup.ui

import com.biwenger_client.helpers.builders.aPlayer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LineupScreenTest {

    @Test
    fun `parseFormation splits the D-M-F string, goalkeeper implicit`() {
        assertThat(parseFormation("3-5-2")).isEqualTo(FormationCounts(defenders = 3, midfielders = 5, forwards = 2))
    }

    @Test
    fun `parseFormation defaults to 0 for a malformed string`() {
        assertThat(parseFormation("not-a-formation")).isEqualTo(FormationCounts(defenders = 0, midfielders = 0, forwards = 0))
    }

    @Test
    fun `withVacantSlots returns players unchanged when the formation's count is already met`() {
        val players = listOf(aPlayer(id = 1), aPlayer(id = 2))

        assertThat(withVacantSlots(players, expectedCount = 2)).isEqualTo(players)
    }

    @Test
    fun `withVacantSlots pads a shortfall with vacant placeholders`() {
        val players = listOf(aPlayer(id = 1))

        val result = withVacantSlots(players, expectedCount = 3)

        assertThat(result).hasSize(3)
        assertThat(result[0]).isEqualTo(players[0])
        assertThat(result.drop(1)).allSatisfy { vacant ->
            assertThat(vacant.name).isEqualTo("?")
            assertThat(vacant.photoUrl).isEqualTo("https://cdn.biwenger.com/i/p/0.png")
        }
    }

    @Test
    fun `withVacantSlots never removes players when there are more than expected`() {
        val players = listOf(aPlayer(id = 1), aPlayer(id = 2), aPlayer(id = 3))

        assertThat(withVacantSlots(players, expectedCount = 1)).isEqualTo(players)
    }
}
