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

    init {
        store.subscribe<Loadable<Lineup>?>(path = "lineup.lineup") {
            it?.let { v -> _lineup.value = v }
        }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(lineupCoeffect),
            handler = ::handleOnLoad
        )

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "lineup.lineup", value = coeffects.load(coeffect = lineupCoeffect)))

    companion object {
        const val ON_LOAD_EVENT = "lineup.on-load"
    }
}
