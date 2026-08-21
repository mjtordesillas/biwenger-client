package com.biwenger_client.features.market.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Store
import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.features.market.domain.coeffects.FetchMarketCoeffect
import com.biwenger_client.features.market.domain.coeffects.FetchMyMarketListingsCoeffect
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.squad.domain.coeffects.FetchMatchDayDetailsCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPerformanceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPriceHistoryCoeffect
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.ui.CURRENT_SEASON
import com.biwenger_client.ui.MatchDayDetailsRequest
import com.biwenger_client.ui.PerformanceHistoryRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Detail-sheet wiring (price/performance history, match-day drill-down)
// mirrors SquadViewModel's — same shared player-detail screen (see
// ui/PlayerDetailScreen.kt), reached from a second entry point now. The
// coeffects it depends on (FetchPriceHistoryCoeffect etc.) live under
// features/squad — reused directly rather than promoted; they're already
// registered globally on the Registry (registration isn't per-feature),
// so no re-registration is needed here, just referencing the same
// coeffect classes.
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val store: Store
) : ViewModel() {

    private val marketCoeffect = FetchMarketCoeffect
    private val myMarketListingsCoeffect = FetchMyMarketListingsCoeffect

    private val _players = mutableStateOf<Loadable<List<MarketListing>>>(Loadable.Loading)
    val players: State<Loadable<List<MarketListing>>> = _players

    private val _myListings = mutableStateOf<Loadable<List<MarketListing>>>(Loadable.Loading)
    val myListings: State<Loadable<List<MarketListing>>> = _myListings

    private val _selectedPlayerId = mutableStateOf<Int?>(null)
    val selectedPlayerId: State<Int?> = _selectedPlayerId

    private val _priceHistory = mutableStateOf<Loadable<PriceHistory>?>(null)
    val priceHistory: State<Loadable<PriceHistory>?> = _priceHistory

    private val _performanceHistory = mutableStateOf<Loadable<PerformanceHistory>?>(null)
    val performanceHistory: State<Loadable<PerformanceHistory>?> = _performanceHistory

    private val _performanceHistorySeason = mutableStateOf(CURRENT_SEASON)
    val performanceHistorySeason: State<String> = _performanceHistorySeason

    private val _selectedMatchDay = mutableStateOf<Int?>(null)
    val selectedMatchDay: State<Int?> = _selectedMatchDay

    private val _matchDayDetails = mutableStateOf<Loadable<MatchDayDetails>?>(null)
    val matchDayDetails: State<Loadable<MatchDayDetails>?> = _matchDayDetails

    init {
        store.subscribe<Loadable<List<MarketListing>>?>(path = "market.players") {
            it?.let { v -> _players.value = v }
        }
        store.subscribe<Loadable<List<MarketListing>>?>(path = "market.myListings") {
            it?.let { v -> _myListings.value = v }
        }
        store.subscribe<Int?>(path = "market.selectedPlayerId") { _selectedPlayerId.value = it }
        store.subscribe<Loadable<PriceHistory>?>(path = "market.priceHistory") { _priceHistory.value = it }
        store.subscribe<Loadable<PerformanceHistory>?>(path = "market.performanceHistory") { _performanceHistory.value = it }
        store.subscribe<String>(path = "market.performanceHistorySeason") { _performanceHistorySeason.value = it }
        store.subscribe<Int?>(path = "market.selectedMatchDay") { _selectedMatchDay.value = it }
        store.subscribe<Loadable<MatchDayDetails>?>(path = "market.matchDayDetails") { _matchDayDetails.value = it }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(marketCoeffect, myMarketListingsCoeffect),
            handler = ::handleOnLoad
        )
        store.registerEventHandler(name = PLAYER_TAPPED_EVENT, handler = ::handlePlayerTapped)
        store.registerEventHandler(
            name = PRICE_HISTORY_REQUESTED_EVENT,
            coeffects = { requestedEvent -> listOf(FetchPriceHistoryCoeffect(playerId = requireNotNull(requestedEvent.payload))) },
            handler = ::handlePriceHistoryRequested
        )
        store.registerEventHandler(
            name = PERFORMANCE_HISTORY_REQUESTED_EVENT,
            coeffects = { requestedEvent ->
                val request = requireNotNull(requestedEvent.payload)
                listOf(FetchPerformanceHistoryCoeffect(playerId = request.playerId, season = request.season))
            },
            handler = ::handlePerformanceHistoryRequested
        )
        store.registerEventHandler(name = PERFORMANCE_SEASON_CHANGED_EVENT, handler = ::handlePerformanceSeasonChanged)
        store.registerEventHandler(name = SHEET_CLOSED_EVENT, handler = ::handleSheetClosed)
        store.registerEventHandler(name = MATCH_DAY_TAPPED_EVENT, handler = ::handleMatchDayTapped)
        store.registerEventHandler(
            name = MATCH_DAY_DETAILS_REQUESTED_EVENT,
            coeffects = { requestedEvent ->
                val request = requireNotNull(requestedEvent.payload)
                listOf(FetchMatchDayDetailsCoeffect(playerId = request.playerId, matchDay = request.matchDay, season = request.season))
            },
            handler = ::handleMatchDayDetailsRequested
        )
        store.registerEventHandler(name = MATCH_DAY_DETAILS_CLOSED_EVENT, handler = ::handleMatchDayDetailsClosed)

        store.dispatch(event = event(name = ON_LOAD_EVENT))
    }

    public override fun onCleared() {
        super.onCleared()
        store.removeEventHandler(name = ON_LOAD_EVENT, handler = ::handleOnLoad)
        store.removeEventHandler(name = PLAYER_TAPPED_EVENT, handler = ::handlePlayerTapped)
        store.removeEventHandler(name = PRICE_HISTORY_REQUESTED_EVENT, handler = ::handlePriceHistoryRequested)
        store.removeEventHandler(name = PERFORMANCE_HISTORY_REQUESTED_EVENT, handler = ::handlePerformanceHistoryRequested)
        store.removeEventHandler(name = PERFORMANCE_SEASON_CHANGED_EVENT, handler = ::handlePerformanceSeasonChanged)
        store.removeEventHandler(name = SHEET_CLOSED_EVENT, handler = ::handleSheetClosed)
        store.removeEventHandler(name = MATCH_DAY_TAPPED_EVENT, handler = ::handleMatchDayTapped)
        store.removeEventHandler(name = MATCH_DAY_DETAILS_REQUESTED_EVENT, handler = ::handleMatchDayDetailsRequested)
        store.removeEventHandler(name = MATCH_DAY_DETAILS_CLOSED_EVENT, handler = ::handleMatchDayDetailsClosed)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(
            UpdateState(path = "market.players", value = coeffects.load(coeffect = marketCoeffect)),
            UpdateState(path = "market.myListings", value = coeffects.load(coeffect = myMarketListingsCoeffect)),
        )

    fun handlePlayerTapped(event: Event<Int>): List<Effect> {
        val playerId = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.selectedPlayerId", value = playerId),
            UpdateState(path = "market.priceHistory", value = Loadable.Loading),
            UpdateState(path = "market.performanceHistory", value = Loadable.Loading),
            UpdateState(path = "market.performanceHistorySeason", value = CURRENT_SEASON),
            DispatchEvent(event = event(name = PRICE_HISTORY_REQUESTED_EVENT, payload = playerId)),
            DispatchEvent(
                event = event(
                    name = PERFORMANCE_HISTORY_REQUESTED_EVENT,
                    payload = PerformanceHistoryRequest(playerId = playerId, season = CURRENT_SEASON)
                )
            ),
        )
    }

    fun handlePriceHistoryRequested(event: Event<Int>, coeffects: Coeffects): List<Effect> =
        listOf(
            UpdateState(
                path = "market.priceHistory",
                value = coeffects.load(coeffect = FetchPriceHistoryCoeffect(playerId = requireNotNull(event.payload)))
            )
        )

    fun handlePerformanceHistoryRequested(event: Event<PerformanceHistoryRequest>, coeffects: Coeffects): List<Effect> {
        val request = requireNotNull(event.payload)
        return listOf(
            UpdateState(
                path = "market.performanceHistory",
                value = coeffects.load(coeffect = FetchPerformanceHistoryCoeffect(playerId = request.playerId, season = request.season))
            )
        )
    }

    fun handlePerformanceSeasonChanged(event: Event<PerformanceHistoryRequest>): List<Effect> {
        val request = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.performanceHistorySeason", value = request.season),
            UpdateState(path = "market.performanceHistory", value = Loadable.Loading),
            DispatchEvent(event = event(name = PERFORMANCE_HISTORY_REQUESTED_EVENT, payload = request)),
        )
    }

    fun handleSheetClosed(event: Event<Unit>): List<Effect> =
        listOf(
            UpdateState(path = "market.selectedPlayerId", value = null),
            UpdateState(path = "market.priceHistory", value = null),
            UpdateState(path = "market.performanceHistory", value = null),
            UpdateState(path = "market.performanceHistorySeason", value = CURRENT_SEASON),
        )

    fun handleMatchDayTapped(event: Event<MatchDayDetailsRequest>): List<Effect> {
        val request = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.selectedMatchDay", value = request.matchDay),
            UpdateState(path = "market.matchDayDetails", value = Loadable.Loading),
            DispatchEvent(event = event(name = MATCH_DAY_DETAILS_REQUESTED_EVENT, payload = request)),
        )
    }

    fun handleMatchDayDetailsRequested(event: Event<MatchDayDetailsRequest>, coeffects: Coeffects): List<Effect> {
        val request = requireNotNull(event.payload)
        return listOf(
            UpdateState(
                path = "market.matchDayDetails",
                value = coeffects.load(
                    coeffect = FetchMatchDayDetailsCoeffect(playerId = request.playerId, matchDay = request.matchDay, season = request.season)
                )
            )
        )
    }

    fun handleMatchDayDetailsClosed(event: Event<Unit>): List<Effect> =
        listOf(
            UpdateState(path = "market.selectedMatchDay", value = null),
            UpdateState(path = "market.matchDayDetails", value = null),
        )

    fun playerTapped(playerId: Int) =
        store.dispatch(event = event(name = PLAYER_TAPPED_EVENT, payload = playerId))

    fun performanceSeasonChanged(playerId: Int, season: String) =
        store.dispatch(
            event = event(
                name = PERFORMANCE_SEASON_CHANGED_EVENT,
                payload = PerformanceHistoryRequest(playerId = playerId, season = season)
            )
        )

    fun sheetClosed() =
        store.dispatch(event = event(name = SHEET_CLOSED_EVENT))

    fun matchDayTapped(playerId: Int, matchDay: Int, season: String) =
        store.dispatch(
            event = event(
                name = MATCH_DAY_TAPPED_EVENT,
                payload = MatchDayDetailsRequest(playerId = playerId, matchDay = matchDay, season = season)
            )
        )

    fun matchDayDetailsClosed() =
        store.dispatch(event = event(name = MATCH_DAY_DETAILS_CLOSED_EVENT))

    companion object {
        const val ON_LOAD_EVENT = "market.on-load"
        const val PLAYER_TAPPED_EVENT = "market.player-tapped"
        const val PRICE_HISTORY_REQUESTED_EVENT = "market.price-history-requested"
        const val PERFORMANCE_HISTORY_REQUESTED_EVENT = "market.performance-history-requested"
        const val PERFORMANCE_SEASON_CHANGED_EVENT = "market.performance-season-changed"
        const val SHEET_CLOSED_EVENT = "market.sheet-closed"
        const val MATCH_DAY_TAPPED_EVENT = "market.match-day-tapped"
        const val MATCH_DAY_DETAILS_REQUESTED_EVENT = "market.match-day-details-requested"
        const val MATCH_DAY_DETAILS_CLOSED_EVENT = "market.match-day-details-closed"
    }
}
