package com.biwenger_client.features.lineup

import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.StateInitializer
import com.biwenger_client.features.lineup.domain.models.BenchCandidates
import com.biwenger_client.features.lineup.domain.models.Lineup

class LineupStateInitializer : StateInitializer {
    override fun initialState(): Map<String, Any?> = mapOf(
        "lineup.lineup" to null as Loadable<Lineup>?,
        "lineup.saveError" to null as Boolean?,
        "lineup.saving" to null as Boolean?,
        "lineup.slotPicker" to null as Loadable<BenchCandidates>?,
    )
}
