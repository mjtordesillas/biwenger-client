package com.biwenger_client.features.squad.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Store
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.features.squad.domain.coeffects.FetchMatchDayDetailsCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPerformanceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPriceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.domain.models.Player
import com.biwenger_client.helpers.builders.aMatchDayDetails
import com.biwenger_client.helpers.builders.aPerformanceHistory
import com.biwenger_client.helpers.builders.aPlayer
import com.biwenger_client.helpers.builders.aPriceHistory
import com.biwenger_client.ui.MatchDayDetailsRequest
import com.biwenger_client.ui.PerformanceHistoryRequest
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
    fun `subscribes to squad_performanceHistory`() {
        verify(store).subscribe(
            eq("squad.performanceHistory"),
            any<(Loadable<com.biwenger_client.features.squad.domain.models.PerformanceHistory>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to squad_performanceHistorySeason`() {
        verify(store).subscribe(eq("squad.performanceHistorySeason"), any<(String) -> Unit>())
    }

    @Test
    fun `subscribes to squad_selectedMatchDay`() {
        verify(store).subscribe(eq("squad.selectedMatchDay"), any<(Int?) -> Unit>())
    }

    @Test
    fun `subscribes to squad_matchDayDetails`() {
        verify(store).subscribe(
            eq("squad.matchDayDetails"),
            any<(Loadable<com.biwenger_client.features.squad.domain.models.MatchDayDetails>?) -> Unit>()
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
            any<suspend (Event<Int>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_price-history-requested handler`() {
        verify(store).registerEventHandler(
            eq("squad.price-history-requested"),
            any<(Event<Int>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_performance-history-requested handler`() {
        verify(store).registerEventHandler(
            eq("squad.performance-history-requested"),
            any<(Event<PerformanceHistoryRequest>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<PerformanceHistoryRequest>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_performance-season-changed handler`() {
        verify(store).registerEventHandler(
            eq("squad.performance-season-changed"),
            any<suspend (Event<PerformanceHistoryRequest>) -> List<Effect>>()
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
    fun `registers squad_match-day-tapped handler`() {
        verify(store).registerEventHandler(
            eq("squad.match-day-tapped"),
            any<suspend (Event<MatchDayDetailsRequest>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_match-day-details-requested handler`() {
        verify(store).registerEventHandler(
            eq("squad.match-day-details-requested"),
            any<(Event<MatchDayDetailsRequest>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<MatchDayDetailsRequest>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers squad_match-day-details-closed handler`() {
        verify(store).registerEventHandler(
            eq("squad.match-day-details-closed"),
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
    fun `handlePlayerTapped sets the selected player id, marks price and performance history loading, resets to current season, and requests both`() {
        val effects = viewModel.handlePlayerTapped(event(name = "squad.player-tapped", payload = 42))

        assertThat(effects).contains(
            UpdateState(path = "squad.selectedPlayerId", value = 42),
            UpdateState(path = "squad.priceHistory", value = Loadable.Loading),
            UpdateState(path = "squad.performanceHistory", value = Loadable.Loading),
            UpdateState(path = "squad.performanceHistorySeason", value = "current"),
            DispatchEvent(event = event(name = "squad.price-history-requested", payload = 42)),
            DispatchEvent(
                event = event(
                    name = "squad.performance-history-requested",
                    payload = PerformanceHistoryRequest(playerId = 42, season = "current")
                )
            ),
        )
    }

    @Test
    fun `handlePriceHistoryRequested returns UpdateState with the loaded price history`() {
        val history = aPriceHistory()
        val coeffects = Coeffects(
            values = mapOf(FetchPriceHistoryCoeffect(playerId = 42) to Loadable.Success(history))
        )

        val effects = viewModel.handlePriceHistoryRequested(
            event(name = "squad.price-history-requested", payload = 42),
            coeffects
        )

        assertThat(effects).contains(
            UpdateState(path = "squad.priceHistory", value = Loadable.Success(history))
        )
    }

    @Test
    fun `handlePerformanceHistoryRequested returns UpdateState with the loaded performance history`() {
        val history = aPerformanceHistory()
        val request = PerformanceHistoryRequest(playerId = 42, season = "previous")
        val coeffects = Coeffects(
            values = mapOf(FetchPerformanceHistoryCoeffect(playerId = 42, season = "previous") to Loadable.Success(history))
        )

        val effects = viewModel.handlePerformanceHistoryRequested(
            event(name = "squad.performance-history-requested", payload = request),
            coeffects
        )

        assertThat(effects).contains(
            UpdateState(path = "squad.performanceHistory", value = Loadable.Success(history))
        )
    }

    @Test
    fun `handlePerformanceSeasonChanged marks performance history loading, records the season, and requests it`() {
        val request = PerformanceHistoryRequest(playerId = 42, season = "previous")

        val effects = viewModel.handlePerformanceSeasonChanged(
            event(name = "squad.performance-season-changed", payload = request)
        )

        assertThat(effects).contains(
            UpdateState(path = "squad.performanceHistorySeason", value = "previous"),
            UpdateState(path = "squad.performanceHistory", value = Loadable.Loading),
            DispatchEvent(event = event(name = "squad.performance-history-requested", payload = request)),
        )
    }

    @Test
    fun `handleSheetClosed clears the selected player id, price history, and performance history`() {
        val effects = viewModel.handleSheetClosed(event(name = "squad.sheet-closed"))

        assertThat(effects).contains(
            UpdateState(path = "squad.selectedPlayerId", value = null),
            UpdateState(path = "squad.priceHistory", value = null),
            UpdateState(path = "squad.performanceHistory", value = null),
            UpdateState(path = "squad.performanceHistorySeason", value = "current"),
        )
    }

    @Test
    fun `handleMatchDayTapped sets the selected match day, marks match day details loading, and requests it`() {
        val request = MatchDayDetailsRequest(playerId = 42, matchDay = 8, season = "current")

        val effects = viewModel.handleMatchDayTapped(event(name = "squad.match-day-tapped", payload = request))

        assertThat(effects).contains(
            UpdateState(path = "squad.selectedMatchDay", value = 8),
            UpdateState(path = "squad.matchDayDetails", value = Loadable.Loading),
            DispatchEvent(event = event(name = "squad.match-day-details-requested", payload = request)),
        )
    }

    @Test
    fun `handleMatchDayDetailsRequested returns UpdateState with the loaded match day details`() {
        val details = aMatchDayDetails()
        val request = MatchDayDetailsRequest(playerId = 42, matchDay = 8, season = "current")
        val coeffects = Coeffects(
            values = mapOf(
                FetchMatchDayDetailsCoeffect(playerId = 42, matchDay = 8, season = "current") to Loadable.Success(details)
            )
        )

        val effects = viewModel.handleMatchDayDetailsRequested(
            event(name = "squad.match-day-details-requested", payload = request),
            coeffects
        )

        assertThat(effects).contains(
            UpdateState(path = "squad.matchDayDetails", value = Loadable.Success(details))
        )
    }

    @Test
    fun `handleMatchDayDetailsClosed clears the selected match day and match day details`() {
        val effects = viewModel.handleMatchDayDetailsClosed(event(name = "squad.match-day-details-closed"))

        assertThat(effects).contains(
            UpdateState(path = "squad.selectedMatchDay", value = null),
            UpdateState(path = "squad.matchDayDetails", value = null),
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
    fun `performanceSeasonChanged dispatches performance-season-changed event`() {
        viewModel.performanceSeasonChanged(playerId = 7, season = "previous")

        verify(store).dispatch(
            event = event(
                name = "squad.performance-season-changed",
                payload = PerformanceHistoryRequest(playerId = 7, season = "previous")
            )
        )
    }

    @Test
    fun `sheetClosed dispatches sheet-closed event`() {
        viewModel.sheetClosed()

        verify(store).dispatch(event = event(name = "squad.sheet-closed"))
    }

    @Test
    fun `matchDayTapped dispatches match-day-tapped event`() {
        viewModel.matchDayTapped(playerId = 7, matchDay = 8, season = "previous")

        verify(store).dispatch(
            event = event(
                name = "squad.match-day-tapped",
                payload = MatchDayDetailsRequest(playerId = 7, matchDay = 8, season = "previous")
            )
        )
    }

    @Test
    fun `matchDayDetailsClosed dispatches match-day-details-closed event`() {
        viewModel.matchDayDetailsClosed()

        verify(store).dispatch(event = event(name = "squad.match-day-details-closed"))
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
            any<suspend (Event<Int>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.price-history-requested"),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.performance-history-requested"),
            any<suspend (Event<PerformanceHistoryRequest>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.performance-season-changed"),
            any<suspend (Event<PerformanceHistoryRequest>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.sheet-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.match-day-tapped"),
            any<suspend (Event<MatchDayDetailsRequest>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.match-day-details-requested"),
            any<suspend (Event<MatchDayDetailsRequest>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("squad.match-day-details-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
    }
}
