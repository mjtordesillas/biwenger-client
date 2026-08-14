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

    init {
        store.subscribe<Loadable<List<Player>>?>(path = "squad.players") {
            it?.let { v -> _players.value = v }
        }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(squadCoeffect),
            handler = ::handleOnLoad
        )

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "squad.players", value = coeffects.load(coeffect = squadCoeffect)))

    companion object {
        const val ON_LOAD_EVENT = "squad.on-load"
    }
}
