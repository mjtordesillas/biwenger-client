package com.biwenger_client.features.lineup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Store
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.features.lineup.domain.coeffects.FetchLineupCoeffect
import com.biwenger_client.features.lineup.domain.effects.SaveLineupEffect
import com.biwenger_client.features.lineup.domain.models.BenchCandidates
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.features.lineup.domain.models.SlotFillRequest
import com.biwenger_client.features.lineup.domain.models.SlotPickerRequest
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.helpers.builders.aLineup
import com.biwenger_client.helpers.builders.aPlayer
import com.biwenger_client.helpers.builders.aSquadPlayer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LineupViewModelTest {

    private var store = mock<Store>()
    private lateinit var viewModel: LineupViewModel
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun beforeEach() {
        viewModelStore = ViewModelStore()
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LineupViewModel(store = store) as T
            }
        }
        viewModel = ViewModelProvider(viewModelStore, factory).get(LineupViewModel::class.java)
    }

    @Test
    fun `subscribes to lineup_lineup`() {
        verify(store).subscribe(
            eq("lineup.lineup"),
            any<(Loadable<Lineup>?) -> Unit>()
        )
    }

    @Test
    fun `registers lineup_on-load handler`() {
        verify(store).registerEventHandler(
            eq("lineup.on-load"),
            any<List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `dispatches on-load event on init`() {
        verify(store).dispatch(event = event(name = "lineup.on-load"))
    }

    @Test
    fun `handleOnLoad returns UpdateState with the loaded lineup`() {
        val lineup = aLineup()
        val coeffects = Coeffects(
            values = mapOf(FetchLineupCoeffect to Loadable.Success(lineup))
        )

        val effects = viewModel.handleOnLoad(event(name = "lineup.on-load"), coeffects)

        assertThat(effects).contains(
            UpdateState(path = "lineup.lineup", value = Loadable.Success(lineup))
        )
    }

    @Test
    fun `removes the on-load handler on cleared`() {
        viewModelStore.clear()

        verify(store).removeEventHandler(
            eq("lineup.on-load"),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `vacateSlot dispatches slot-vacated with the player id`() {
        viewModel.vacateSlot(41101)

        verify(store).dispatch(event = event(name = "lineup.slot-vacated", payload = 41101))
    }

    @Test
    fun `registers lineup_slot-vacated handler`() {
        verify(store).registerEventHandler(
            eq("lineup.slot-vacated"),
            any<List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
    }

    // Regression guard for the null-in-place fix: nulling the tapped
    // player's id must not shift any other slot, whether it was a real
    // player or an already-vacant (null) one.
    @Test
    fun `handleSlotVacated nulls only the tapped player's id, keeping every other slot's index`() {
        val goalkeeper = aPlayer(id = 41101)
        val defender = aPlayer(id = 2)
        val lineup = aLineup(players = listOf(goalkeeper, null, defender))
        val coeffects = Coeffects(values = mapOf(FetchLineupCoeffect to Loadable.Success(lineup)))

        val effects = viewModel.handleSlotVacated(event(name = "lineup.slot-vacated", payload = 41101), coeffects)

        assertThat(effects).containsExactly(
            UpdateState(path = "lineup.saving", value = true),
            UpdateState(path = "lineup.saveError", value = false),
            SaveLineupEffect(formation = "3-5-2", playerIds = listOf(null, null, 2)),
        )
    }

    @Test
    fun `handleSlotVacated is a no-op when the current lineup isn't loaded`() {
        val coeffects = Coeffects(values = mapOf(FetchLineupCoeffect to Loadable.Loading))

        val effects = viewModel.handleSlotVacated(event(name = "lineup.slot-vacated", payload = 41101), coeffects)

        assertThat(effects).isEmpty()
    }

    @Test
    fun `handleSaveSucceeded updates the lineup, clears the save error, and stops saving`() {
        val lineup: Lineup? = aLineup(formation = "4-4-2")

        val effects = viewModel.handleSaveSucceeded(event(name = "lineup.save-succeeded", payload = lineup))

        assertThat(effects).containsExactly(
            UpdateState(path = "lineup.lineup", value = Loadable.Success(lineup)),
            UpdateState(path = "lineup.saveError", value = false),
            UpdateState(path = "lineup.saving", value = false),
        )
    }

    @Test
    fun `handleSaveFailed sets the save error and stops saving, leaving the lineup untouched`() {
        val effects = viewModel.handleSaveFailed(event(name = "lineup.save-failed"))

        assertThat(effects).containsExactly(
            UpdateState(path = "lineup.saveError", value = true),
            UpdateState(path = "lineup.saving", value = false),
        )
    }

    @Test
    fun `requestBenchOptions dispatches vacant-slot-tapped with the slot`() {
        val slot = LineupSlot(index = 3, position = 2, player = aPlayer(id = 0, name = "?"))

        viewModel.requestBenchOptions(slot)

        verify(store).dispatch(event = event(name = "lineup.vacant-slot-tapped", payload = slot))
    }

    @Test
    fun `handleSlotTapped shows Loading immediately, then requests the picker via DispatchEvent`() {
        val slot = LineupSlot(index = 3, position = 2, player = aPlayer(id = 0, name = "?"))

        val effects = viewModel.handleSlotTapped(event(name = "lineup.vacant-slot-tapped", payload = slot))

        assertThat(effects).containsExactly(
            UpdateState(path = "lineup.slotPicker", value = Loadable.Loading),
            UpdateState(path = "lineup.saveError", value = false),
            DispatchEvent(
                event = event(
                    name = "lineup.slot-picker-requested",
                    payload = SlotPickerRequest(index = 3, position = 2)
                )
            ),
        )
    }

    @Test
    fun `handleSlotPickerRequested splits bench players into specialists (primary match) and jollies (secondary match), excluding starters`() {
        val startingGoalkeeper = aPlayer(id = 41101, position = 1)
        val lineup = aLineup(players = listOf(startingGoalkeeper), credits = 20)
        val benchGoalkeeper = aSquadPlayer(id = 2, name = "Bench GK", position = 1)
        val ownGoalkeeperAlreadyStarting = aSquadPlayer(id = 41101, name = "Starting GK", position = 1)
        val benchDefender = aSquadPlayer(id = 3, name = "Bench DF", position = 2)
        // A MF/FW aligned as GK isn't realistic football, but exercises
        // the secondary-position match same as any other band would.
        val jollyGoalkeeper = aSquadPlayer(id = 4, name = "Jolly", position = 3, secondaryPosition = 1)
        val coeffects = Coeffects(
            values = mapOf(
                FetchLineupCoeffect to Loadable.Success(lineup),
                FetchSquadCoeffect to Loadable.Success(
                    listOf(benchGoalkeeper, ownGoalkeeperAlreadyStarting, benchDefender, jollyGoalkeeper)
                ),
            )
        )

        val effects = viewModel.handleSlotPickerRequested(
            event(name = "lineup.slot-picker-requested", payload = SlotPickerRequest(index = 0, position = 1)),
            coeffects
        )

        assertThat(effects).containsExactly(
            UpdateState(
                path = "lineup.slotPicker",
                value = Loadable.Success(
                    BenchCandidates(
                        slotIndex = 0,
                        specialists = listOf(benchGoalkeeper),
                        jollies = listOf(jollyGoalkeeper),
                        canAffordJolly = true,
                    )
                )
            )
        )
    }

    @Test
    fun `handleSlotPickerRequested sets canAffordJolly false when short of the off-position credit cost`() {
        val lineup = aLineup(players = listOf(aPlayer(id = 41101, position = 1)), credits = 1)
        val jolly = aSquadPlayer(id = 4, name = "Jolly", position = 3, secondaryPosition = 1)
        val coeffects = Coeffects(
            values = mapOf(
                FetchLineupCoeffect to Loadable.Success(lineup),
                FetchSquadCoeffect to Loadable.Success(listOf(jolly)),
            )
        )

        val effects = viewModel.handleSlotPickerRequested(
            event(name = "lineup.slot-picker-requested", payload = SlotPickerRequest(index = 0, position = 1)),
            coeffects
        )

        val picker = (effects.single() as UpdateState).value as Loadable.Success<*>
        assertThat((picker.value as BenchCandidates).canAffordJolly).isFalse()
    }

    @Test
    fun `closeSlotPicker dispatches slot-picker-closed`() {
        viewModel.closeSlotPicker()

        verify(store).dispatch(event = event(name = "lineup.slot-picker-closed"))
    }

    @Test
    fun `handleSlotPickerClosed clears the picker`() {
        val effects = viewModel.handleSlotPickerClosed(event(name = "lineup.slot-picker-closed"))

        assertThat(effects).containsExactly(UpdateState(path = "lineup.slotPicker", value = null))
    }

    @Test
    fun `fillSlot dispatches slot-filled with the index and chosen player id`() {
        viewModel.fillSlot(3, 41412)

        verify(store).dispatch(
            event = event(name = "lineup.slot-filled", payload = SlotFillRequest(index = 3, playerId = 41412))
        )
    }

    // Regression guard, same as handleSlotVacated: filling only the
    // targeted index must leave every other slot's id (real or
    // already-vacant) untouched.
    @Test
    fun `handleSlotFilled sets only the targeted index, keeping every other slot as-is`() {
        val goalkeeper = aPlayer(id = 41101)
        val lineup = aLineup(players = listOf(goalkeeper, null))
        val coeffects = Coeffects(values = mapOf(FetchLineupCoeffect to Loadable.Success(lineup)))

        val effects = viewModel.handleSlotFilled(
            event(name = "lineup.slot-filled", payload = SlotFillRequest(index = 1, playerId = 8747)),
            coeffects
        )

        assertThat(effects).containsExactly(
            UpdateState(path = "lineup.saving", value = true),
            UpdateState(path = "lineup.saveError", value = false),
            SaveLineupEffect(formation = "3-5-2", playerIds = listOf(41101, 8747)),
        )
    }

    @Test
    fun `handleSlotFilled is a no-op when the current lineup isn't loaded`() {
        val coeffects = Coeffects(values = mapOf(FetchLineupCoeffect to Loadable.Loading))

        val effects = viewModel.handleSlotFilled(
            event(name = "lineup.slot-filled", payload = SlotFillRequest(index = 1, playerId = 8747)),
            coeffects
        )

        assertThat(effects).isEmpty()
    }

    @Test
    fun `changeFormation dispatches formation-changed with the chosen formation`() {
        viewModel.changeFormation("4-4-2")

        verify(store).dispatch(event = event(name = "lineup.formation-changed", payload = "4-4-2"))
    }

    @Test
    fun `registers lineup_formation-changed handler`() {
        verify(store).registerEventHandler(
            eq("lineup.formation-changed"),
            any<List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<String>, Coeffects) -> List<Effect>>()
        )
    }

    // Regression guard, same reasoning as handleSlotVacated/Filled: the
    // saved formation and reshaped playerIds come from a just-fetched
    // lineup, not a possibly-stale local copy.
    @Test
    fun `handleFormationChanged reshapes the current eleven for the new formation`() {
        val goalkeeper = aPlayer(id = 1)
        val defenders = listOf(aPlayer(id = 2), aPlayer(id = 3), aPlayer(id = 4))
        val midfielders = listOf(aPlayer(id = 5), aPlayer(id = 6), aPlayer(id = 7), aPlayer(id = 8), aPlayer(id = 9))
        val forwards = listOf(aPlayer(id = 10), aPlayer(id = 11))
        val lineup = aLineup(formation = "3-5-2", players = listOf(goalkeeper) + defenders + midfielders + forwards)
        val coeffects = Coeffects(values = mapOf(FetchLineupCoeffect to Loadable.Success(lineup)))

        val effects = viewModel.handleFormationChanged(
            event(name = "lineup.formation-changed", payload = "4-4-2"),
            coeffects
        )

        assertThat(effects).containsExactly(
            UpdateState(path = "lineup.saving", value = true),
            UpdateState(path = "lineup.saveError", value = false),
            SaveLineupEffect(
                formation = "4-4-2",
                playerIds = listOf(1, 2, 3, 4, null, 5, 6, 7, 8, 10, 11)
            ),
        )
    }

    @Test
    fun `handleFormationChanged is a no-op when the current lineup isn't loaded`() {
        val coeffects = Coeffects(values = mapOf(FetchLineupCoeffect to Loadable.Loading))

        val effects = viewModel.handleFormationChanged(
            event(name = "lineup.formation-changed", payload = "4-4-2"),
            coeffects
        )

        assertThat(effects).isEmpty()
    }
}
