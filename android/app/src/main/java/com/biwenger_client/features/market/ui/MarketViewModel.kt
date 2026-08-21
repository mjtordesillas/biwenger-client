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
import com.biwenger_client.features.market.domain.coeffects.FetchBidsCoeffect
import com.biwenger_client.features.market.domain.coeffects.FetchOffersCoeffect
import com.biwenger_client.features.market.domain.effects.AcceptOfferEffect
import com.biwenger_client.features.market.domain.effects.OFFER_ACCEPTANCE_FINISHED_EVENT
import com.biwenger_client.features.market.domain.effects.OFFER_REJECTION_FINISHED_EVENT
import com.biwenger_client.features.market.domain.effects.RejectOfferEffect
import com.biwenger_client.features.market.domain.effects.UNLIST_PLAYER_FINISHED_EVENT
import com.biwenger_client.features.market.domain.effects.UnlistPlayerEffect
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
import com.biwenger_client.features.market.domain.models.PlayerOffer
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
    private val offersCoeffect = FetchOffersCoeffect
    private val bidsCoeffect = FetchBidsCoeffect

    private val _players = mutableStateOf<Loadable<List<MarketListing>>>(Loadable.Loading)
    val players: State<Loadable<List<MarketListing>>> = _players

    private val _myListings = mutableStateOf<Loadable<List<MarketListing>>>(Loadable.Loading)
    val myListings: State<Loadable<List<MarketListing>>> = _myListings

    private val _offers = mutableStateOf<Loadable<List<PlayerOffer>>>(Loadable.Loading)
    val offers: State<Loadable<List<PlayerOffer>>> = _offers

    private val _bids = mutableStateOf<Loadable<List<PlayerBid>>>(Loadable.Loading)
    val bids: State<Loadable<List<PlayerBid>>> = _bids

    private val _offerToReject = mutableStateOf<PlayerOffer?>(null)
    val offerToReject: State<PlayerOffer?> = _offerToReject

    private val _rejectingOffer = mutableStateOf(false)
    val rejectingOffer: State<Boolean> = _rejectingOffer

    private val _offerToAccept = mutableStateOf<PlayerOffer?>(null)
    val offerToAccept: State<PlayerOffer?> = _offerToAccept

    private val _acceptingOffer = mutableStateOf(false)
    val acceptingOffer: State<Boolean> = _acceptingOffer

    private val _unlistingPlayerIds = mutableStateOf<Set<Int>>(emptySet())
    val unlistingPlayerIds: State<Set<Int>> = _unlistingPlayerIds

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
        store.subscribe<Loadable<List<PlayerOffer>>?>(path = "market.offers") {
            it?.let { v -> _offers.value = v }
        }
        store.subscribe<Loadable<List<PlayerBid>>?>(path = "market.bids") {
            it?.let { v -> _bids.value = v }
        }
        store.subscribe<PlayerOffer?>(path = "market.offerToReject") { _offerToReject.value = it }
        store.subscribe<Boolean?>(path = "market.rejectingOffer") { it?.let { v -> _rejectingOffer.value = v } }
        store.subscribe<PlayerOffer?>(path = "market.offerToAccept") { _offerToAccept.value = it }
        store.subscribe<Boolean?>(path = "market.acceptingOffer") { it?.let { v -> _acceptingOffer.value = v } }
        store.subscribe<Set<Int>?>(path = "market.unlistingPlayerIds") { it?.let { v -> _unlistingPlayerIds.value = v } }
        store.subscribe<Int?>(path = "market.selectedPlayerId") { _selectedPlayerId.value = it }
        store.subscribe<Loadable<PriceHistory>?>(path = "market.priceHistory") { _priceHistory.value = it }
        store.subscribe<Loadable<PerformanceHistory>?>(path = "market.performanceHistory") { _performanceHistory.value = it }
        store.subscribe<String>(path = "market.performanceHistorySeason") { _performanceHistorySeason.value = it }
        store.subscribe<Int?>(path = "market.selectedMatchDay") { _selectedMatchDay.value = it }
        store.subscribe<Loadable<MatchDayDetails>?>(path = "market.matchDayDetails") { _matchDayDetails.value = it }

        store.registerEventHandler(
            name = ON_LOAD_EVENT,
            coeffects = listOf(marketCoeffect, myMarketListingsCoeffect, offersCoeffect, bidsCoeffect),
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
        store.registerEventHandler(name = OFFER_REJECTION_OPENED_EVENT, handler = ::handleOfferRejectionOpened)
        store.registerEventHandler(name = OFFER_REJECTION_CANCELLED_EVENT, handler = ::handleOfferRejectionCancelled)
        store.registerEventHandler(name = OFFER_REJECTION_REQUESTED_EVENT, handler = ::handleOfferRejectionRequested)
        store.registerEventHandler(
            name = OFFER_REJECTION_FINISHED_EVENT,
            coeffects = listOf(offersCoeffect),
            handler = ::handleOfferRejectionFinished
        )
        store.registerEventHandler(name = OFFER_ACCEPTANCE_OPENED_EVENT, handler = ::handleOfferAcceptanceOpened)
        store.registerEventHandler(name = OFFER_ACCEPTANCE_CANCELLED_EVENT, handler = ::handleOfferAcceptanceCancelled)
        store.registerEventHandler(name = OFFER_ACCEPTANCE_REQUESTED_EVENT, handler = ::handleOfferAcceptanceRequested)
        store.registerEventHandler(
            name = OFFER_ACCEPTANCE_FINISHED_EVENT,
            coeffects = listOf(offersCoeffect),
            handler = ::handleOfferAcceptanceFinished
        )
        store.registerEventHandler(name = UNLIST_PLAYER_REQUESTED_EVENT, handler = ::handleUnlistPlayerRequested)
        store.registerEventHandler(
            name = UNLIST_PLAYER_FINISHED_EVENT,
            coeffects = listOf(myMarketListingsCoeffect),
            handler = ::handleUnlistPlayerFinished
        )

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
        store.removeEventHandler(name = OFFER_REJECTION_OPENED_EVENT, handler = ::handleOfferRejectionOpened)
        store.removeEventHandler(name = OFFER_REJECTION_CANCELLED_EVENT, handler = ::handleOfferRejectionCancelled)
        store.removeEventHandler(name = OFFER_REJECTION_REQUESTED_EVENT, handler = ::handleOfferRejectionRequested)
        store.removeEventHandler(name = OFFER_REJECTION_FINISHED_EVENT, handler = ::handleOfferRejectionFinished)
        store.removeEventHandler(name = OFFER_ACCEPTANCE_OPENED_EVENT, handler = ::handleOfferAcceptanceOpened)
        store.removeEventHandler(name = OFFER_ACCEPTANCE_CANCELLED_EVENT, handler = ::handleOfferAcceptanceCancelled)
        store.removeEventHandler(name = OFFER_ACCEPTANCE_REQUESTED_EVENT, handler = ::handleOfferAcceptanceRequested)
        store.removeEventHandler(name = OFFER_ACCEPTANCE_FINISHED_EVENT, handler = ::handleOfferAcceptanceFinished)
        store.removeEventHandler(name = UNLIST_PLAYER_REQUESTED_EVENT, handler = ::handleUnlistPlayerRequested)
        store.removeEventHandler(name = UNLIST_PLAYER_FINISHED_EVENT, handler = ::handleUnlistPlayerFinished)
    }

    fun handleOnLoad(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(
            UpdateState(path = "market.players", value = coeffects.load(coeffect = marketCoeffect)),
            UpdateState(path = "market.myListings", value = coeffects.load(coeffect = myMarketListingsCoeffect)),
            UpdateState(path = "market.offers", value = coeffects.load(coeffect = offersCoeffect)),
            UpdateState(path = "market.bids", value = coeffects.load(coeffect = bidsCoeffect)),
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

    fun handleOfferRejectionOpened(event: Event<PlayerOffer>): List<Effect> =
        listOf(UpdateState(path = "market.offerToReject", value = requireNotNull(event.payload)))

    fun handleOfferRejectionCancelled(event: Event<Unit>): List<Effect> =
        listOf(UpdateState(path = "market.offerToReject", value = null))

    fun handleOfferRejectionRequested(event: Event<PlayerOffer>): List<Effect> {
        val offer = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.rejectingOffer", value = true),
            RejectOfferEffect(offerId = offer.offerId),
        )
    }

    fun handleOfferRejectionFinished(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(
            UpdateState(path = "market.offerToReject", value = null),
            UpdateState(path = "market.rejectingOffer", value = false),
            UpdateState(path = "market.offers", value = coeffects.load(coeffect = offersCoeffect)),
        )

    fun handleOfferAcceptanceOpened(event: Event<PlayerOffer>): List<Effect> =
        listOf(UpdateState(path = "market.offerToAccept", value = requireNotNull(event.payload)))

    fun handleOfferAcceptanceCancelled(event: Event<Unit>): List<Effect> =
        listOf(UpdateState(path = "market.offerToAccept", value = null))

    fun handleOfferAcceptanceRequested(event: Event<PlayerOffer>): List<Effect> {
        val offer = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.acceptingOffer", value = true),
            AcceptOfferEffect(offerId = offer.offerId),
        )
    }

    fun handleOfferAcceptanceFinished(event: Event<Unit>, coeffects: Coeffects): List<Effect> =
        listOf(
            UpdateState(path = "market.offerToAccept", value = null),
            UpdateState(path = "market.acceptingOffer", value = false),
            UpdateState(path = "market.offers", value = coeffects.load(coeffect = offersCoeffect)),
        )

    // No dialog gates this one — multiple rows could plausibly be tapped
    // in quick succession, so this is a set of in-flight ids rather than
    // a single id/boolean, computed off the ViewModel's own already-synced
    // mirror of it (safe here since each request/finished round-trip is
    // a fully separate event dispatch, not a concurrent read).
    fun handleUnlistPlayerRequested(event: Event<Int>): List<Effect> {
        val playerId = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.unlistingPlayerIds", value = _unlistingPlayerIds.value + playerId),
            UnlistPlayerEffect(playerId = playerId),
        )
    }

    fun handleUnlistPlayerFinished(event: Event<Int>, coeffects: Coeffects): List<Effect> {
        val playerId = requireNotNull(event.payload)
        return listOf(
            UpdateState(path = "market.unlistingPlayerIds", value = _unlistingPlayerIds.value - playerId),
            UpdateState(path = "market.myListings", value = coeffects.load(coeffect = myMarketListingsCoeffect)),
        )
    }

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

    fun openOfferRejection(offer: PlayerOffer) =
        store.dispatch(event = event(name = OFFER_REJECTION_OPENED_EVENT, payload = offer))

    fun cancelOfferRejection() =
        store.dispatch(event = event(name = OFFER_REJECTION_CANCELLED_EVENT))

    fun rejectOffer(offer: PlayerOffer) =
        store.dispatch(event = event(name = OFFER_REJECTION_REQUESTED_EVENT, payload = offer))

    fun openOfferAcceptance(offer: PlayerOffer) =
        store.dispatch(event = event(name = OFFER_ACCEPTANCE_OPENED_EVENT, payload = offer))

    fun cancelOfferAcceptance() =
        store.dispatch(event = event(name = OFFER_ACCEPTANCE_CANCELLED_EVENT))

    fun acceptOffer(offer: PlayerOffer) =
        store.dispatch(event = event(name = OFFER_ACCEPTANCE_REQUESTED_EVENT, payload = offer))

    fun unlistPlayer(listing: MarketListing) =
        store.dispatch(event = event(name = UNLIST_PLAYER_REQUESTED_EVENT, payload = listing.id))

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
        const val OFFER_REJECTION_OPENED_EVENT = "market.offer-rejection-opened"
        const val OFFER_REJECTION_CANCELLED_EVENT = "market.offer-rejection-cancelled"
        const val OFFER_REJECTION_REQUESTED_EVENT = "market.offer-rejection-requested"
        const val OFFER_ACCEPTANCE_OPENED_EVENT = "market.offer-acceptance-opened"
        const val OFFER_ACCEPTANCE_CANCELLED_EVENT = "market.offer-acceptance-cancelled"
        const val OFFER_ACCEPTANCE_REQUESTED_EVENT = "market.offer-acceptance-requested"
        const val UNLIST_PLAYER_REQUESTED_EVENT = "market.unlist-player-requested"
    }
}
