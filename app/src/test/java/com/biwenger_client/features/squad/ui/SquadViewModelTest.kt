package com.biwenger_client.features.squad.ui

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
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.features.squad.domain.models.Player
import com.biwenger_client.helpers.builders.aPlayer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SquadViewModelTest {

    private var store = mock<Store>()
    private lateinit var viewModel: SquadViewModel
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun beforeEach() {
        viewModelStore = ViewModelStore()
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SquadViewModel(store = store) as T
            }
        }
        viewModel = ViewModelProvider(viewModelStore, factory).get(SquadViewModel::class.java)
    }

    @Test
    fun `subscribes to squad_players`() {
        verify(store).subscribe(
            eq("squad.players"),
            any<(Loadable<List<Player>>?) -> Unit>()
        )
    }

    @Test
    fun `registers squad_on-load handler`() {
        verify(store).registerEventHandler(
            eq("squad.on-load"),
            any<List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `dispatches on-load event on init`() {
        verify(store).dispatch(event = event(name = "squad.on-load"))
    }

    @Test
    fun `handleOnLoad returns UpdateState with loaded players`() {
        val players = listOf(aPlayer())
        val coeffects = Coeffects(
            values = mapOf(FetchSquadCoeffect to Loadable.Success(players))
        )

        val effects = viewModel.handleOnLoad(event(name = "squad.on-load"), coeffects)

        assertThat(effects).contains(
            UpdateState(path = "squad.players", value = Loadable.Success(players))
        )
    }

    @Test
    fun `removes on-load handler on cleared`() {
        viewModelStore.clear()

        verify(store).removeEventHandler(
            eq("squad.on-load"),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
    }
}
