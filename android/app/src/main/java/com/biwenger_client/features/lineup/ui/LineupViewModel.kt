package com.biwenger_client.features.lineup.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Store
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.features.lineup.domain.coeffects.FetchLineupCoeffect
import com.biwenger_client.features.lineup.domain.effects.LINEUP_SAVE_FAILED_EVENT
import com.biwenger_client.features.lineup.domain.effects.LINEUP_SAVE_SUCCEEDED_EVENT
import com.biwenger_client.features.lineup.domain.effects.SaveLineupEffect
import com.biwenger_client.features.lineup.domain.models.BenchCandidates
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.features.lineup.domain.models.OffPositionCreditCost
import com.biwenger_client.features.lineup.domain.models.SlotFillRequest
import com.biwenger_client.features.lineup.domain.models.SlotPickerRequest
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LineupViewModel @Inject constructor(
    private val store: Store
) : ViewModel() {

    private val lineupCoeffect = FetchLineupCoeffect
    private val squadCoeffect = FetchSquadCoeffect

    private val _lineup = mutableStateOf<Loadable<Lineup>>(Loadable.Loading)
    val lineup: State<Loadable<Lineup>> = _lineup

    private val _saveError = mutableStateOf(false)
    val saveError: State<Boolean> = _saveError

    private val _saving = mutableStateOf(false)
    val saving: State<Boolean> = _saving

    private val _slotPicker = mutableStateOf<Loadable<BenchCandidates>?>(null)
    val slotPicker: State<Loadable<BenchCandidates>?> = _slotPicker

    init {
        store.subscribe<Loadable<Lineup>?>(path = "lineup.lineup") {
            it?.let { v -> _lineup.value = v }
        }
        store.subscribe<Boolean?>(path = "lineup.saveError") {
            it?.let { v -> _saveError.value = v }
        }
        store.subscribe<Boolean?>(path = "lineup.saving") {
            it?.let { v -> _saving.value = v }
        }
        store.subscribe<Loadable<BenchCandidates>?>(path = "lineup.slotPicker") { _slotPicker.value = it }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(lineupCoeffect),
            handler = ::handleOnLoad
        )
        store.registerEventHandler(
            name = SLOT_VACATED_EVENT,
            coeffects = listOf(lineupCoeffect),
            handler = ::handleSlotVacated
        )
        store.registerEventHandler(name = LINEUP_SAVE_SUCCEEDED_EVENT, handler = ::handleSaveSucceeded)
        store.registerEventHandler(name = LINEUP_SAVE_FAILED_EVENT, handler = ::handleSaveFailed)
        store.registerEventHandler(name = SLOT_TAPPED_EVENT, handler = ::handleSlotTapped)
        store.registerEventHandler(
            name = SLOT_PICKER_REQUESTED_EVENT,
            coeffects = listOf(lineupCoeffect, squadCoeffect),
            handler = ::handleSlotPickerRequested
        )
        store.registerEventHandler(name = SLOT_PICKER_CLOSED_EVENT, handler = ::handleSlotPickerClosed)
        store.registerEventHandler(
            name = SLOT_FILLED_EVENT,
            coeffects = listOf(lineupCoeffect),
            handler = ::handleSlotFilled
        )
        store.registerEventHandler(
            name = FORMATION_CHANGED_EVENT,
            coeffects = listOf(lineupCoeffect),
            handler = ::handleFormationChanged
        )
        store.registerEventHandler(name = REFRESH_REQUESTED_EVENT, handler = ::handleRefreshRequested)

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
        store.removeEventHandler(name = SLOT_VACATED_EVENT, handler = ::handleSlotVacated)
        store.removeEventHandler(name = LINEUP_SAVE_SUCCEEDED_EVENT, handler = ::handleSaveSucceeded)
        store.removeEventHandler(name = LINEUP_SAVE_FAILED_EVENT, handler = ::handleSaveFailed)
        store.removeEventHandler(name = SLOT_TAPPED_EVENT, handler = ::handleSlotTapped)
        store.removeEventHandler(name = SLOT_PICKER_REQUESTED_EVENT, handler = ::handleSlotPickerRequested)
        store.removeEventHandler(name = SLOT_PICKER_CLOSED_EVENT, handler = ::handleSlotPickerClosed)
        store.removeEventHandler(name = SLOT_FILLED_EVENT, handler = ::handleSlotFilled)
        store.removeEventHandler(name = FORMATION_CHANGED_EVENT, handler = ::handleFormationChanged)
        store.removeEventHandler(name = REFRESH_REQUESTED_EVENT, handler = ::handleRefreshRequested)
    }

    // Benches a starter with no replacement — see
    // docs/backlog/to-do/swap-lineup-players.md and
    // docs/biwenger-api-notes.md § "Starting lineup — write". Only
    // dispatches; the actual playersID rebuild is handleSlotVacated's
    // job, not this action method's (see docs/coding-conventions/viewmodels.md).
    fun vacateSlot(playerId: Int) {
        store.dispatch(event = event(name = SLOT_VACATED_EVENT, payload = playerId))
    }

    // Opens the bench picker for a tapped vacant slot.
    fun requestBenchOptions(slot: LineupSlot) {
        store.dispatch(event = event(name = SLOT_TAPPED_EVENT, payload = slot))
    }

    fun closeSlotPicker() {
        store.dispatch(event = event(name = SLOT_PICKER_CLOSED_EVENT))
    }

    // `index`/`playerId` come straight from what's already on screen
    // (the open picker's slot, the tapped candidate) — relaying them is
    // not the "logic" docs/coding-conventions/viewmodels.md reserves for
    // event handlers, same as vacateSlot passing a bare id through.
    fun fillSlot(index: Int, playerId: Int) {
        store.dispatch(event = event(name = SLOT_FILLED_EVENT, payload = SlotFillRequest(index = index, playerId = playerId)))
    }

    // `formation` is one of FreeFormations, chosen straight from the
    // picker — no logic here, same reasoning as vacateSlot/fillSlot.
    fun changeFormation(formation: String) {
        store.dispatch(event = event(name = FORMATION_CHANGED_EVENT, payload = formation))
    }

    fun refresh() {
        store.dispatch(event = event(name = REFRESH_REQUESTED_EVENT))
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "lineup.lineup", value = coeffects.load(coeffect = lineupCoeffect)))

    // Same two-step Loading-then-DispatchEvent pattern as
    // SquadViewModel's handleRefreshRequested — blanks back to Loading
    // before re-triggering ON_LOAD_EVENT, so pull-to-refresh shows the
    // same full-screen spinner first load does.
    fun handleRefreshRequested(event: Event<Unit>): List<Effect> =
        listOf(
            UpdateState(path = "lineup.lineup", value = Loadable.Loading),
            DispatchEvent(event = event(name = ON_LOAD_EVENT)),
        )

    // Re-fetches the lineup (via the same coeffect handleOnLoad uses)
    // rather than trusting the ViewModel's already-displayed state, so
    // the array sent back to Biwenger reflects what it actually has
    // right now, not a possibly-stale local copy.
    fun handleSlotVacated(event: Event<Int>, coeffects: Coeffects): List<Effect> {
        val current = coeffects.load(coeffect = lineupCoeffect)
        val lineup = (current as? Loadable.Success)?.value ?: return emptyList()
        val playerIds = lineup.players.map { player -> if (player?.id == event.payload) null else player?.id }
        return listOf(
            UpdateState(path = "lineup.saving", value = true),
            UpdateState(path = "lineup.saveError", value = false),
            SaveLineupEffect(formation = lineup.formation, playerIds = playerIds),
        )
    }

    fun handleSaveSucceeded(event: Event<Lineup?>): List<Effect> = listOf(
        UpdateState(
            path = "lineup.lineup",
            value = Loadable.Success(value = event.payload ?: Lineup(formation = "", players = emptyList(), credits = 0))
        ),
        UpdateState(path = "lineup.saveError", value = false),
        UpdateState(path = "lineup.saving", value = false),
    )

    fun handleSaveFailed(event: Event<Unit>): List<Effect> =
        listOf(
            UpdateState(path = "lineup.saveError", value = true),
            UpdateState(path = "lineup.saving", value = false),
        )

    // Shows the picker as Loading immediately, same two-step
    // Loading-then-DispatchEvent pattern SquadViewModel's
    // handlePlayerTapped/PRICE_HISTORY_REQUESTED_EVENT already uses —
    // computing the actual candidate list needs coeffects (a fresh
    // lineup + squad fetch), which a plain, coeffect-less handler can't
    // load itself. Clears any saveError left over from a previous
    // attempt on a different slot — otherwise this new dialog would
    // open straight onto that stale error instead of the picker.
    fun handleSlotTapped(event: Event<LineupSlot>): List<Effect> {
        val slot = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "lineup.slotPicker", value = Loadable.Loading),
            UpdateState(path = "lineup.saveError", value = false),
            DispatchEvent(
                event = event(
                    name = SLOT_PICKER_REQUESTED_EVENT,
                    payload = SlotPickerRequest(index = slot.index, position = slot.position)
                )
            )
        )
    }

    // Eligible = on the bench (owned, not currently in the eleven), same
    // primary position as the tapped slot's band ("specialists") or
    // that band only as a secondary position ("jollies") — see
    // BenchCandidates. `canAffordJolly` gates jolly cards in the UI
    // rather than blocking them here, so the picker can still show a
    // manager what a jolly *would* look like even short on credits.
    fun handleSlotPickerRequested(event: Event<SlotPickerRequest>, coeffects: Coeffects): List<Effect> {
        val request = requireNotNull(event.payload)
        val lineupResult = coeffects.load(coeffect = lineupCoeffect)
        val squadResult = coeffects.load(coeffect = squadCoeffect)
        val picker = when {
            lineupResult is Loadable.Failed -> lineupResult
            squadResult is Loadable.Failed -> squadResult
            lineupResult is Loadable.Success && squadResult is Loadable.Success -> {
                val startingIds = lineupResult.value.players.mapNotNull { it?.id }.toSet()
                val bench = squadResult.value.filter { it.id !in startingIds }
                Loadable.Success(
                    BenchCandidates(
                        slotIndex = request.index,
                        specialists = bench.filter { it.position == request.position },
                        jollies = bench.filter { it.position != request.position && it.secondaryPosition == request.position },
                        canAffordJolly = lineupResult.value.credits >= OffPositionCreditCost,
                    )
                )
            }
            else -> Loadable.Loading
        }
        return listOf(UpdateState(path = "lineup.slotPicker", value = picker))
    }

    fun handleSlotPickerClosed(event: Event<Unit>): List<Effect> =
        listOf(UpdateState(path = "lineup.slotPicker", value = null))

    // Same freshness reasoning as handleSlotVacated: rebuilds playerIds
    // off a just-fetched lineup, not the ViewModel's displayed copy.
    // Doesn't clear the picker itself — the dialog stays open showing a
    // saving state until handleSaveSucceeded/Failed land, then the UI
    // closes it (see LineupContent's awaitingSave).
    fun handleSlotFilled(event: Event<SlotFillRequest>, coeffects: Coeffects): List<Effect> {
        val request = requireNotNull(event.payload)
        val current = coeffects.load(coeffect = lineupCoeffect)
        val lineup = (current as? Loadable.Success)?.value ?: return emptyList()
        val playerIds = lineup.players.mapIndexed { index, player -> if (index == request.index) request.playerId else player?.id }
        return listOf(
            UpdateState(path = "lineup.saving", value = true),
            UpdateState(path = "lineup.saveError", value = false),
            SaveLineupEffect(formation = lineup.formation, playerIds = playerIds),
        )
    }

    // Same freshness reasoning as handleSlotVacated/handleSlotFilled:
    // reshapes off a just-fetched lineup, not the ViewModel's displayed
    // copy. See reshapeLineup for the carry-over/bench/vacate rule.
    fun handleFormationChanged(event: Event<String>, coeffects: Coeffects): List<Effect> {
        val newFormation = requireNotNull(event.payload)
        val current = coeffects.load(coeffect = lineupCoeffect)
        val lineup = (current as? Loadable.Success)?.value ?: return emptyList()
        val playerIds = reshapeLineup(players = lineup.players, currentFormation = lineup.formation, newFormation = newFormation)
        return listOf(
            UpdateState(path = "lineup.saving", value = true),
            UpdateState(path = "lineup.saveError", value = false),
            SaveLineupEffect(formation = newFormation, playerIds = playerIds),
        )
    }

    companion object {
        const val ON_LOAD_EVENT = "lineup.on-load"
        const val SLOT_VACATED_EVENT = "lineup.slot-vacated"
        const val SLOT_TAPPED_EVENT = "lineup.vacant-slot-tapped"
        const val SLOT_PICKER_REQUESTED_EVENT = "lineup.slot-picker-requested"
        const val SLOT_PICKER_CLOSED_EVENT = "lineup.slot-picker-closed"
        const val SLOT_FILLED_EVENT = "lineup.slot-filled"
        const val FORMATION_CHANGED_EVENT = "lineup.formation-changed"
        const val REFRESH_REQUESTED_EVENT = "lineup.refresh-requested"
    }
}
