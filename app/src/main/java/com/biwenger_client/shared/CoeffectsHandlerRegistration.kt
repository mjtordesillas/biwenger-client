package com.biwenger_client.shared

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.squad.SquadCoeffectsHandlerRegistration
import com.biwenger_client.features.squad.infrastructure.SquadService

class CoeffectsHandlerRegistration(
    private val registry: Registry,
    private val squadService: SquadService,
) {
    fun register() {
        SquadCoeffectsHandlerRegistration(registry = registry, squadService = squadService).register()
    }
}
