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

    // Regression: a real vacancy is `null` in place, not a shorter list
    // (see docs/biwenger-api-notes.md § "Starting lineup — write",
    // confirmed against a real account). Filtering it out upstream used
    // to collapse the list and shift every later slot's band — this
    // checks the placeholder lands at the vacant index itself instead.
    @Test
    fun `withVacantSlots turns a null entry into the vacant placeholder, in place`() {
        val player = aPlayer(id = 1)

        val result = withVacantSlots(listOf(null, player), expectedCount = 2)

        assertThat(result[0].name).isEqualTo("?")
        assertThat(result[0].photoUrl).isEqualTo("https://cdn.biwenger.com/i/p/0.png")
        assertThat(result[1]).isEqualTo(player)
    }

    // Regression: a player aligned in their secondary position (e.g. a
    // MF/FW played as a forward, for extra Biwenger credits) must land
    // in the band they're actually playing, not their catalogue
    // `position` — only `players`' order (goalkeeper, then D/M/F
    // counts) tells the two apart.
    @Test
    fun `sliceLineupBands follows list order, not each player's catalogue position`() {
        val goalkeeper = aPlayer(id = 1, position = 1)
        val defender = aPlayer(id = 2, position = 2)
        // A MF/FW aligned as a forward: catalogue position is MF (3),
        // but they're standing in the forward slot.
        val midfielderPlayedAsForward = aPlayer(id = 3, position = 3)
        val midfielder = aPlayer(id = 4, position = 3)

        val bands = sliceLineupBands(
            players = listOf(goalkeeper, defender, midfielder, midfielderPlayedAsForward),
            counts = FormationCounts(defenders = 1, midfielders = 1, forwards = 1)
        )

        assertThat(bands.goalkeepers.map { it.player }).containsExactly(goalkeeper)
        assertThat(bands.defenders.map { it.player }).containsExactly(defender)
        assertThat(bands.midfielders.map { it.player }).containsExactly(midfielder)
        assertThat(bands.forwards.map { it.player }).containsExactly(midfielderPlayedAsForward)
    }

    // A fill/vacate write needs the slot's true index into `players`
    // (there's no player id to key off a vacant slot) and its band's
    // catalogue position code (eligibility filtering) — both travel
    // with each LineupSlot regardless of which band it lands in.
    @Test
    fun `sliceLineupBands carries each slot's true index and band position code`() {
        val goalkeeper = aPlayer(id = 1, position = 1)
        val defender = aPlayer(id = 2, position = 2)
        val midfielder = aPlayer(id = 3, position = 3)
        val forward = aPlayer(id = 4, position = 4)

        val bands = sliceLineupBands(
            players = listOf(goalkeeper, defender, midfielder, forward),
            counts = FormationCounts(defenders = 1, midfielders = 1, forwards = 1)
        )

        assertThat(bands.goalkeepers.single()).isEqualTo(LineupSlot(index = 0, position = 1, player = goalkeeper))
        assertThat(bands.defenders.single()).isEqualTo(LineupSlot(index = 1, position = 2, player = defender))
        assertThat(bands.midfielders.single()).isEqualTo(LineupSlot(index = 2, position = 3, player = midfielder))
        assertThat(bands.forwards.single()).isEqualTo(LineupSlot(index = 3, position = 4, player = forward))
    }

    // Regression: a `null` mid-list (a real vacancy) must be consumed as
    // one element of whichever band it falls in, same as a real player,
    // so it can't shift a later band's slice — this is the actual bug
    // the write-endpoint spike surfaced (see docs/biwenger-api-notes.md
    // § "Starting lineup").
    @Test
    fun `sliceLineupBands treats a null entry as a vacant slot without shifting later bands`() {
        val goalkeeper = aPlayer(id = 1, position = 1)
        val defender = aPlayer(id = 2, position = 2)
        val forward = aPlayer(id = 3, position = 4)

        val bands = sliceLineupBands(
            players = listOf(goalkeeper, null, defender, forward),
            counts = FormationCounts(defenders = 2, midfielders = 0, forwards = 1)
        )

        assertThat(bands.goalkeepers.map { it.player }).containsExactly(goalkeeper)
        assertThat(bands.defenders[0].player.name).isEqualTo("?")
        assertThat(bands.defenders[1].player).isEqualTo(defender)
        assertThat(bands.forwards.map { it.player }).containsExactly(forward)
    }

    @Test
    fun `sliceLineupBands pads each band independently when players run short`() {
        val goalkeeper = aPlayer(id = 1, position = 1)
        val defender = aPlayer(id = 2, position = 2)

        val bands = sliceLineupBands(
            players = listOf(goalkeeper, defender),
            counts = FormationCounts(defenders = 2, midfielders = 1, forwards = 1)
        )

        assertThat(bands.goalkeepers.map { it.player }).containsExactly(goalkeeper)
        assertThat(bands.defenders.map { it.player.id }).containsExactly(2, 0)
        assertThat(bands.midfielders.single().player.name).isEqualTo("?")
        assertThat(bands.forwards.single().player.name).isEqualTo("?")
    }

    // change-lineup-formation's core rule: a band that grows pads with
    // vacant (null) slots, a band that shrinks drops the overflow
    // (benched, not deleted — reshapeLineup only returns ids), a band
    // that's unchanged carries over untouched, and the goalkeeper always
    // carries over regardless of either formation.
    @Test
    fun `reshapeLineup pads a growing band, drops overflow from a shrinking band, keeps an unchanged band`() {
        val goalkeeper = aPlayer(id = 1)
        val defenders = listOf(aPlayer(id = 2), aPlayer(id = 3), aPlayer(id = 4))
        val midfielders = listOf(aPlayer(id = 5), aPlayer(id = 6), aPlayer(id = 7), aPlayer(id = 8), aPlayer(id = 9))
        val forwards = listOf(aPlayer(id = 10), aPlayer(id = 11))
        val players = listOf(goalkeeper) + defenders + midfielders + forwards

        val playerIds = reshapeLineup(players = players, currentFormation = "3-5-2", newFormation = "4-4-2")

        assertThat(playerIds).containsExactly(
            1, // goalkeeper, unchanged
            2, 3, 4, null, // 3 defenders carried over, 1 new vacant slot
            5, 6, 7, 8, // first 4 of 5 midfielders carried over, the 5th dropped (benched)
            10, 11, // forwards unchanged (2 -> 2)
        )
    }

    @Test
    fun `reshapeLineup carries the goalkeeper regardless of formation`() {
        val players = listOf(aPlayer(id = 1)) + List(10) { null }

        val playerIds = reshapeLineup(players = players, currentFormation = "5-3-2", newFormation = "3-4-3")

        assertThat(playerIds.first()).isEqualTo(1)
    }

    // An already-vacant slot isn't a player to carry over or drop —
    // reshapeLineup treats it exactly like withVacantSlots does: `null`
    // in, `null` (or dropped, if the band shrinks) out.
    @Test
    fun `reshapeLineup treats an existing vacancy as nothing to carry over`() {
        val players = listOf(aPlayer(id = 1), null, aPlayer(id = 3))

        val playerIds = reshapeLineup(
            players = players,
            currentFormation = "1-1-0", // not a real Biwenger formation, just exercises the band math
            newFormation = "1-1-0"
        )

        assertThat(playerIds).containsExactly(1, null, 3)
    }
}
