package com.biwenger_client.features.squad

import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.StateInitializer
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.Player
import com.biwenger_client.features.squad.domain.models.PriceHistory

class SquadStateInitializer : StateInitializer {
    override fun initialState(): Map<String, Any?> = mapOf(
        "squad.players" to null as Loadable<List<Player>>?,
        "squad.selectedPosition" to null as Int?,
        "squad.selectedPlayerId" to null as Int?,
        "squad.priceHistory" to null as Loadable<PriceHistory>?,
        "squad.performanceHistory" to null as Loadable<PerformanceHistory>?,
        "squad.performanceHistorySeason" to "current",
        "squad.selectedMatchDay" to null as Int?,
        "squad.matchDayDetails" to null as Loadable<MatchDayDetails>?,
    )
}
