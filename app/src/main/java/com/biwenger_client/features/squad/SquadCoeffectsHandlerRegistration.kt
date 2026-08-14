package com.biwenger_client.features.squad

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffectHandler
import com.biwenger_client.features.squad.infrastructure.SquadService

class SquadCoeffectsHandlerRegistration(
    private val registry: Registry,
    private val squadService: SquadService,
) {
    fun register() {
        registry.registerCoeffectHandler(
            coeffectClass = FetchSquadCoeffect::class,
            handler = FetchSquadCoeffectHandler(squadService = squadService)
        )
    }
}
