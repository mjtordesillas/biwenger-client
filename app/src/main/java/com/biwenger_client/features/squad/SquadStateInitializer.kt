package com.biwenger_client.features.squad

import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.StateInitializer
import com.biwenger_client.features.squad.domain.models.Player

class SquadStateInitializer : StateInitializer {
    override fun initialState(): Map<String, Any?> = mapOf(
        "squad.players" to null as Loadable<List<Player>>?,
    )
}
