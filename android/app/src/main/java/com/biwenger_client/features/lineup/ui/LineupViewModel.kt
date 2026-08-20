package com.biwenger_client.features.lineup.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.biwenger_client.core.coeffects.Coeffects
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
import com.biwenger_client.features.lineup.domain.models.Lineup
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LineupViewModel @Inject constructor(
    private val store: Store
) : ViewModel() {

    private val lineupCoeffect = FetchLineupCoeffect

    private val _lineup = mutableStateOf<Loadable<Lineup>>(Loadable.Loading)
    val lineup: State<Loadable<Lineup>> = _lineup

    private val _saveError = mutableStateOf(false)
    val saveError: State<Boolean> = _saveError

    init {
        store.subscribe<Loadable<Lineup>?>(path = "lineup.lineup") {
            it?.let { v -> _lineup.value = v }
        }
        store.subscribe<Boolean?>(path = "lineup.saveError") {
            it?.let { v -> _saveError.value = v }
        }

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

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
        store.removeEventHandler(name = SLOT_VACATED_EVENT, handler = ::handleSlotVacated)
        store.removeEventHandler(name = LINEUP_SAVE_SUCCEEDED_EVENT, handler = ::handleSaveSucceeded)
        store.removeEventHandler(name = LINEUP_SAVE_FAILED_EVENT, handler = ::handleSaveFailed)
    }

    // Benches a starter with no replacement — see
    // docs/backlog/to-do/swap-lineup-players.md and
    // docs/biwenger-api-notes.md § "Starting lineup — write". Only
    // dispatches; the actual playersID rebuild is handleSlotVacated's
    // job, not this action method's (see docs/coding-conventions/viewmodels.md).
    fun vacateSlot(playerId: Int) {
        store.dispatch(event = event(name = SLOT_VACATED_EVENT, payload = playerId))
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "lineup.lineup", value = coeffects.load(coeffect = lineupCoeffect)))

    // Re-fetches the lineup (via the same coeffect handleOnLoad uses)
    // rather than trusting the ViewModel's already-displayed state, so
    // the array sent back to Biwenger reflects what it actually has
    // right now, not a possibly-stale local copy.
    fun handleSlotVacated(event: Event<Int>, coeffects: Coeffects): List<Effect> {
        val current = coeffects.load(coeffect = lineupCoeffect)
        val lineup = (current as? Loadable.Success)?.value ?: return emptyList()
        val playerIds = lineup.players.map { player -> if (player?.id == event.payload) null else player?.id }
        return listOf(SaveLineupEffect(formation = lineup.formation, playerIds = playerIds))
    }

    fun handleSaveSucceeded(event: Event<Lineup?>): List<Effect> = listOf(
        UpdateState(
            path = "lineup.lineup",
            value = Loadable.Success(value = event.payload ?: Lineup(formation = "", players = emptyList()))
        ),
        UpdateState(path = "lineup.saveError", value = false),
    )

    fun handleSaveFailed(event: Event<Unit>): List<Effect> =
        listOf(UpdateState(path = "lineup.saveError", value = true))

    companion object {
        const val ON_LOAD_EVENT = "lineup.on-load"
        const val SLOT_VACATED_EVENT = "lineup.slot-vacated"
    }
}
