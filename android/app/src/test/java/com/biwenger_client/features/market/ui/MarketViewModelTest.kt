package com.biwenger_client.features.market.ui

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
import com.biwenger_client.features.market.domain.coeffects.FetchBidsCoeffect
import com.biwenger_client.features.market.domain.coeffects.FetchMarketCoeffect
import com.biwenger_client.features.market.domain.coeffects.FetchMyMarketListingsCoeffect
import com.biwenger_client.features.market.domain.coeffects.FetchOffersCoeffect
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
import com.biwenger_client.features.market.domain.models.PlayerOffer
import com.biwenger_client.features.squad.domain.coeffects.FetchMatchDayDetailsCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPerformanceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPriceHistoryCoeffect
import com.biwenger_client.helpers.builders.aMarketListing
import com.biwenger_client.helpers.builders.aMatchDayDetails
import com.biwenger_client.helpers.builders.aPerformanceHistory
import com.biwenger_client.helpers.builders.aPlayerBid
import com.biwenger_client.helpers.builders.aPlayerOffer
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
            any<(Loadable<List<MarketListing>>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to market_myListings`() {
        verify(store).subscribe(
            eq("market.myListings"),
            any<(Loadable<List<MarketListing>>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to market_offers`() {
        verify(store).subscribe(
            eq("market.offers"),
            any<(Loadable<List<PlayerOffer>>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to market_bids`() {
        verify(store).subscribe(
            eq("market.bids"),
            any<(Loadable<List<PlayerBid>>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to market_selectedPlayerId`() {
        verify(store).subscribe(eq("market.selectedPlayerId"), any<(Int?) -> Unit>())
    }

    @Test
    fun `subscribes to market_priceHistory`() {
        verify(store).subscribe(
            eq("market.priceHistory"),
            any<(Loadable<com.biwenger_client.features.squad.domain.models.PriceHistory>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to market_performanceHistory`() {
        verify(store).subscribe(
            eq("market.performanceHistory"),
            any<(Loadable<com.biwenger_client.features.squad.domain.models.PerformanceHistory>?) -> Unit>()
        )
    }

    @Test
    fun `subscribes to market_performanceHistorySeason`() {
        verify(store).subscribe(eq("market.performanceHistorySeason"), any<(String) -> Unit>())
    }

    @Test
    fun `subscribes to market_selectedMatchDay`() {
        verify(store).subscribe(eq("market.selectedMatchDay"), any<(Int?) -> Unit>())
    }

    @Test
    fun `subscribes to market_matchDayDetails`() {
        verify(store).subscribe(
            eq("market.matchDayDetails"),
            any<(Loadable<com.biwenger_client.features.squad.domain.models.MatchDayDetails>?) -> Unit>()
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
    fun `registers market_player-tapped handler`() {
        verify(store).registerEventHandler(
            eq("market.player-tapped"),
            any<suspend (Event<Int>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_price-history-requested handler`() {
        verify(store).registerEventHandler(
            eq("market.price-history-requested"),
            any<(Event<Int>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_performance-history-requested handler`() {
        verify(store).registerEventHandler(
            eq("market.performance-history-requested"),
            any<(Event<PerformanceHistoryRequest>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<PerformanceHistoryRequest>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_performance-season-changed handler`() {
        verify(store).registerEventHandler(
            eq("market.performance-season-changed"),
            any<suspend (Event<PerformanceHistoryRequest>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_sheet-closed handler`() {
        verify(store).registerEventHandler(
            eq("market.sheet-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_match-day-tapped handler`() {
        verify(store).registerEventHandler(
            eq("market.match-day-tapped"),
            any<suspend (Event<MatchDayDetailsRequest>) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_match-day-details-requested handler`() {
        verify(store).registerEventHandler(
            eq("market.match-day-details-requested"),
            any<(Event<MatchDayDetailsRequest>) -> List<com.biwenger_client.core.coeffects.Coeffect<*>>>(),
            any<suspend (Event<MatchDayDetailsRequest>, Coeffects) -> List<Effect>>()
        )
    }

    @Test
    fun `registers market_match-day-details-closed handler`() {
        verify(store).registerEventHandler(
            eq("market.match-day-details-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
    }

    @Test
    fun `dispatches on-load event on init`() {
        verify(store).dispatch(event = event(name = "market.on-load"))
    }

    @Test
    fun `handleOnLoad returns UpdateState with loaded players, my listings, offers, and bids`() {
        val listings = listOf(aMarketListing())
        val myListings = listOf(aMarketListing(id = 2))
        val offers = listOf(aPlayerOffer(id = 3))
        val bids = listOf(aPlayerBid(id = 4))
        val coeffects = Coeffects(
            values = mapOf(
                FetchMarketCoeffect to Loadable.Success(listings),
                FetchMyMarketListingsCoeffect to Loadable.Success(myListings),
                FetchOffersCoeffect to Loadable.Success(offers),
                FetchBidsCoeffect to Loadable.Success(bids),
            )
        )

        val effects = viewModel.handleOnLoad(event(name = "market.on-load"), coeffects)

        assertThat(effects).contains(
            UpdateState(path = "market.players", value = Loadable.Success(listings)),
            UpdateState(path = "market.myListings", value = Loadable.Success(myListings)),
            UpdateState(path = "market.offers", value = Loadable.Success(offers)),
            UpdateState(path = "market.bids", value = Loadable.Success(bids)),
        )
    }

    @Test
    fun `handlePlayerTapped sets the selected player id, marks price and performance history loading, resets to current season, and requests both`() {
        val effects = viewModel.handlePlayerTapped(event(name = "market.player-tapped", payload = 42))

        assertThat(effects).contains(
            UpdateState(path = "market.selectedPlayerId", value = 42),
            UpdateState(path = "market.priceHistory", value = Loadable.Loading),
            UpdateState(path = "market.performanceHistory", value = Loadable.Loading),
            UpdateState(path = "market.performanceHistorySeason", value = "current"),
            DispatchEvent(event = event(name = "market.price-history-requested", payload = 42)),
            DispatchEvent(
                event = event(
                    name = "market.performance-history-requested",
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
            event(name = "market.price-history-requested", payload = 42),
            coeffects
        )

        assertThat(effects).contains(
            UpdateState(path = "market.priceHistory", value = Loadable.Success(history))
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
            event(name = "market.performance-history-requested", payload = request),
            coeffects
        )

        assertThat(effects).contains(
            UpdateState(path = "market.performanceHistory", value = Loadable.Success(history))
        )
    }

    @Test
    fun `handlePerformanceSeasonChanged marks performance history loading, records the season, and requests it`() {
        val request = PerformanceHistoryRequest(playerId = 42, season = "previous")

        val effects = viewModel.handlePerformanceSeasonChanged(
            event(name = "market.performance-season-changed", payload = request)
        )

        assertThat(effects).contains(
            UpdateState(path = "market.performanceHistorySeason", value = "previous"),
            UpdateState(path = "market.performanceHistory", value = Loadable.Loading),
            DispatchEvent(event = event(name = "market.performance-history-requested", payload = request)),
        )
    }

    @Test
    fun `handleSheetClosed clears the selected player id, price history, and performance history`() {
        val effects = viewModel.handleSheetClosed(event(name = "market.sheet-closed"))

        assertThat(effects).contains(
            UpdateState(path = "market.selectedPlayerId", value = null),
            UpdateState(path = "market.priceHistory", value = null),
            UpdateState(path = "market.performanceHistory", value = null),
            UpdateState(path = "market.performanceHistorySeason", value = "current"),
        )
    }

    @Test
    fun `handleMatchDayTapped sets the selected match day, marks match day details loading, and requests it`() {
        val request = MatchDayDetailsRequest(playerId = 42, matchDay = 8, season = "current")

        val effects = viewModel.handleMatchDayTapped(event(name = "market.match-day-tapped", payload = request))

        assertThat(effects).contains(
            UpdateState(path = "market.selectedMatchDay", value = 8),
            UpdateState(path = "market.matchDayDetails", value = Loadable.Loading),
            DispatchEvent(event = event(name = "market.match-day-details-requested", payload = request)),
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
            event(name = "market.match-day-details-requested", payload = request),
            coeffects
        )

        assertThat(effects).contains(
            UpdateState(path = "market.matchDayDetails", value = Loadable.Success(details))
        )
    }

    @Test
    fun `handleMatchDayDetailsClosed clears the selected match day and match day details`() {
        val effects = viewModel.handleMatchDayDetailsClosed(event(name = "market.match-day-details-closed"))

        assertThat(effects).contains(
            UpdateState(path = "market.selectedMatchDay", value = null),
            UpdateState(path = "market.matchDayDetails", value = null),
        )
    }

    @Test
    fun `playerTapped dispatches player-tapped event`() {
        viewModel.playerTapped(playerId = 7)

        verify(store).dispatch(event = event(name = "market.player-tapped", payload = 7))
    }

    @Test
    fun `performanceSeasonChanged dispatches performance-season-changed event`() {
        viewModel.performanceSeasonChanged(playerId = 7, season = "previous")

        verify(store).dispatch(
            event = event(
                name = "market.performance-season-changed",
                payload = PerformanceHistoryRequest(playerId = 7, season = "previous")
            )
        )
    }

    @Test
    fun `sheetClosed dispatches sheet-closed event`() {
        viewModel.sheetClosed()

        verify(store).dispatch(event = event(name = "market.sheet-closed"))
    }

    @Test
    fun `matchDayTapped dispatches match-day-tapped event`() {
        viewModel.matchDayTapped(playerId = 7, matchDay = 8, season = "previous")

        verify(store).dispatch(
            event = event(
                name = "market.match-day-tapped",
                payload = MatchDayDetailsRequest(playerId = 7, matchDay = 8, season = "previous")
            )
        )
    }

    @Test
    fun `matchDayDetailsClosed dispatches match-day-details-closed event`() {
        viewModel.matchDayDetailsClosed()

        verify(store).dispatch(event = event(name = "market.match-day-details-closed"))
    }

    @Test
    fun `removes all handlers on cleared`() {
        viewModelStore.clear()

        verify(store).removeEventHandler(
            eq("market.on-load"),
            any<suspend (Event<Unit>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.player-tapped"),
            any<suspend (Event<Int>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.price-history-requested"),
            any<suspend (Event<Int>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.performance-history-requested"),
            any<suspend (Event<PerformanceHistoryRequest>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.performance-season-changed"),
            any<suspend (Event<PerformanceHistoryRequest>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.sheet-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.match-day-tapped"),
            any<suspend (Event<MatchDayDetailsRequest>) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.match-day-details-requested"),
            any<suspend (Event<MatchDayDetailsRequest>, Coeffects) -> List<Effect>>()
        )
        verify(store).removeEventHandler(
            eq("market.match-day-details-closed"),
            any<suspend (Event<Unit>) -> List<Effect>>()
        )
    }
}
