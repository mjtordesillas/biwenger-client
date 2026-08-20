package com.biwenger_client.features.lineup.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.lineup.infrastructure.LineupService
import com.biwenger_client.infrastructure.network.Response

// Event names a LineupViewModel registers plain (coeffect-less) handlers
// for, to turn the write's result into UpdateState effects — declared
// here, not on the ViewModel, so this domain-layer handler doesn't
// depend on the ui layer to dispatch its own follow-up.
const val LINEUP_SAVE_SUCCEEDED_EVENT = "lineup.save-succeeded"
const val LINEUP_SAVE_FAILED_EVENT = "lineup.save-failed"

// Writes {formation, playerIds} against Biwenger — see
// docs/biwenger-api-notes.md § "Starting lineup — write". Doesn't
// return further Effects itself (EffectHandler can't); instead
// dispatches a follow-up event, same pattern DispatchEventHandler
// already uses, letting a plain event handler turn the result into an
// UpdateState effect.
data class SaveLineupEffect(val formation: String, val playerIds: List<Int?>) : Effect

class SaveLineupEffectHandler(
    private val lineupService: LineupService,
    private val registry: Registry,
) : EffectHandler<SaveLineupEffect> {
    override suspend fun handle(effect: SaveLineupEffect) {
        when (val result = lineupService.saveLineup(formation = effect.formation, playerIds = effect.playerIds)) {
            is Response.Success -> registry.dispatch(event = event(name = LINEUP_SAVE_SUCCEEDED_EVENT, payload = result.body))
            is Response.Error -> registry.dispatch(event = event(name = LINEUP_SAVE_FAILED_EVENT))
        }
    }
}
