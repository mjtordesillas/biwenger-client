package com.biwenger_client.features.market.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Store
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.domain.models.Player
import com.biwenger_client.features.market.domain.coeffects.FetchMarketCoeffect
import com.biwenger_client.helpers.builders.aPlayer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MarketViewModelTest {

    private var store = mock<Store>()
    private lateinit var viewModel: MarketViewModel
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun beforeEach() {
        viewModelStore = ViewModelStore()
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MarketViewModel(store = store) as T
            }
        }
        viewModel = ViewModelProvider(viewModelStore, factory).get(MarketViewModel::class.java)
    }

    @Test
    fun `subscribes to market_players`() {
        verify(store).subscribe(
            eq("market.players"),
            any<(Loadable<List<Player>>?) -> Unit>()
        )
    }

    @Test
    fun `registers market_on-load handler`() {
        verify(store).registerEventHandler(
            eq("market.on-load"),
            any<List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `dispatches on-load event on init`() {
        verify(store).dispatch(event = event(name = "market.on-load"))
    }

    @Test
    fun `handleOnLoad returns UpdateState with loaded players`() {
        val players = listOf(aPlayer())
        val coeffects = Coeffects(
            values = mapOf(FetchMarketCoeffect to Loadable.Success(players))
        )

        val effects = viewModel.handleOnLoad(event(name = "market.on-load"), coeffects)

        assertThat(effects).contains(
            UpdateState(path = "market.players", value = Loadable.Success(players))
        )
    }
}
