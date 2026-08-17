package com.biwenger_client.features.market.ui

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
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.market.domain.coeffects.FetchMarketCoeffect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val store: Store
) : ViewModel() {

    private val marketCoeffect = FetchMarketCoeffect

    private val _players = mutableStateOf<Loadable<List<Player>>>(Loadable.Loading)
    val players: State<Loadable<List<Player>>> = _players

    init {
        store.subscribe<Loadable<List<Player>>?>(path = "market.players") {
            it?.let { v -> _players.value = v }
        }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(marketCoeffect),
            handler = ::handleOnLoad
        )

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(UpdateState(path = "market.players", value = coeffects.load(coeffect = marketCoeffect)))

    companion object {
        const val ON_LOAD_EVENT = "market.on-load"
    }
}
