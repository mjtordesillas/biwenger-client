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
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.features.lineup.domain.coeffects.FetchLineupCoeffect
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.helpers.builders.aPlayer
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
        val lineup = Lineup(formation = "3-5-2", players = listOf(aPlayer()))
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
}
