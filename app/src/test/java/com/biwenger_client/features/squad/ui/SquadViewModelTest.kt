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
import com.biwenger_client.features.squad.domain.coeffects.FetchPriceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.features.squad.domain.models.Player
import com.biwenger_client.helpers.builders.aPlayer
import com.biwenger_client.helpers.builders.aPriceHistory
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
    fun `subscribes to squad_selectedPosition`() {
        verify(store).subscribe(eq("squad.selectedPosition"), any<(Int?) -> Unit>())
    }

    @Test
    fun `subscribes to squad_selectedPlayerId`() {
        verify(store).subscribe(eq("squad.selectedPlayerId"), any<(Int?) -> Unit>())
    }

    @Test
    fun `subscribes to squad_priceHistory`() {
        verify(store).subscribe(
            eq("squad.priceHistory"),
            any<(Loadable<com.biwenger_client.features.squad.domain.models.PriceHistory>?) -> Unit>()
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
    fun `registers squad_position-filter-changed handler`() {
        verify(store).registerEventHandler(
            eq("squad.position-filter-changed"),
            any<suspend (Event<Int?>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_player-tapped handler`() {
        verify(store).registerEventHandler(
            eq("squad.player-tapped"),
            any<(Event<Int>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_sheet-closed handler`() {
        verify(store).registerEventHandler(
            eq("squad.sheet-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
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
    fun `handlePositionFilterChanged returns UpdateState with the new position`() {
        val effects = viewModel.handlePositionFilterChanged(event(name = "squad.position-filter-changed", payload = 3))

        assertThat(effects).contains(UpdateState(path = "squad.selectedPosition", value = 3))
    }

    @Test
    fun `handlePositionFilterChanged with null payload clears the filter`() {
        val effects = viewModel.handlePositionFilterChanged(
            Event(name = "squad.position-filter-changed", payload = null)
        )

        assertThat(effects).contains(UpdateState(path = "squad.selectedPosition", value = null))
    }

    @Test
    fun `handlePlayerTapped returns UpdateState with the tapped player id and loaded price history`() {
        val history = aPriceHistory()
        val coeffects = Coeffects(
            values = mapOf(FetchPriceHistoryCoeffect(playerId = 42) to Loadable.Success(history))
        )

        val effects = viewModel.handlePlayerTapped(event(name = "squad.player-tapped", payload = 42), coeffects)

        assertThat(effects).contains(
            UpdateState(path = "squad.selectedPlayerId", value = 42),
            UpdateState(path = "squad.priceHistory", value = Loadable.Success(history)),
        )
    }

    @Test
    fun `handleSheetClosed clears the selected player id and price history`() {
        val effects = viewModel.handleSheetClosed(event(name = "squad.sheet-closed"))

        assertThat(effects).contains(
            UpdateState(path = "squad.selectedPlayerId", value = null),
            UpdateState(path = "squad.priceHistory", value = null),
        )
    }

    @Test
    fun `positionFilterChanged dispatches position-filter-changed event`() {
        viewModel.positionFilterChanged(position = 2)

        verify(store).dispatch(event = event(name = "squad.position-filter-changed", payload = 2))
    }

    @Test
    fun `playerTapped dispatches player-tapped event`() {
        viewModel.playerTapped(playerId = 7)

        verify(store).dispatch(event = event(name = "squad.player-tapped", payload = 7))
    }

    @Test
    fun `sheetClosed dispatches sheet-closed event`() {
        viewModel.sheetClosed()

        verify(store).dispatch(event = event(name = "squad.sheet-closed"))
    }

    @Test
    fun `removes all handlers on cleared`() {
        viewModelStore.clear()

        verify(store).removeEventHandler(
            eq("squad.on-load"),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.position-filter-changed"),
            any<suspend (Event<Int?>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.player-tapped"),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.sheet-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
    }
}
