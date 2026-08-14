package com.biwenger_client.features.squad.ui

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
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.features.squad.domain.models.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SquadViewModel @Inject constructor(
    private val store: Store
) : ViewModel() {

    private val squadCoeffect = FetchSquadCoeffect

    private val _players = mutableStateOf<Loadable<List<Player>>>(Loadable.Loading)
    val players: State<Loadable<List<Player>>> = _players

    private val _selectedPosition = mutableStateOf<Int?>(null)
    val selectedPosition: State<Int?> = _selectedPosition

    private val _selectedPlayerId = mutableStateOf<Int?>(null)
    val selectedPlayerId: State<Int?> = _selectedPlayerId

    init {
        store.subscribe<Loadable<List<Player>>?>(path = "squad.players") {
            it?.let { v -> _players.value = v }
        }
        store.subscribe<Int?>(path = "squad.selectedPosition") { _selectedPosition.value = it }
        store.subscribe<Int?>(path = "squad.selectedPlayerId") { _selectedPlayerId.value = it }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(squadCoeffect),
            handler = ::handleOnLoad
        )
        store.registerEventHandler(name = POSITION_FILTER_CHANGED_EVENT, handler = ::handlePositionFilterChanged)
        store.registerEventHandler(name = PLAYER_TAPPED_EVENT, handler = ::handlePlayerTapped)
        store.registerEventHandler(name = SHEET_CLOSED_EVENT, handler = ::handleSheetClosed)

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
        store.removeEventHandler(name = POSITION_FILTER_CHANGED_EVENT, handler = ::handlePositionFilterChanged)
        store.removeEventHandler(name = PLAYER_TAPPED_EVENT, handler = ::handlePlayerTapped)
        store.removeEventHandler(name = SHEET_CLOSED_EVENT, handler = ::handleSheetClosed)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "squad.players", value = coeffects.load(coeffect = squadCoeffect)))

    fun handlePositionFilterChanged(event: Event<Int?>): List<Effect> =
        listOf(UpdateState(path = "squad.selectedPosition", value = event.payload))

    fun handlePlayerTapped(event: Event<Int>): List<Effect> =
        listOf(UpdateState(path = "squad.selectedPlayerId", value = event.payload))

    fun handleSheetClosed(event: Event<Unit>): List<Effect> =
        listOf(UpdateState(path = "squad.selectedPlayerId", value = null))

    fun positionFilterChanged(position: Int?) =
        store.dispatch(event = event(name = POSITION_FILTER_CHANGED_EVENT, payload = position))

    fun playerTapped(playerId: Int) =
        store.dispatch(event = event(name = PLAYER_TAPPED_EVENT, payload = playerId))

    fun sheetClosed() =
        store.dispatch(event = event(name = SHEET_CLOSED_EVENT))

    companion object {
        const val ON_LOAD_EVENT = "squad.on-load"
        const val POSITION_FILTER_CHANGED_EVENT = "squad.position-filter-changed"
        const val PLAYER_TAPPED_EVENT = "squad.player-tapped"
        const val SHEET_CLOSED_EVENT = "squad.sheet-closed"
    }
}
