package com.biwenger_client.core.state

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler

data class UpdateState(
    val path: String,
    val value: Any?
) : Effect

class UpdateStateHandler(
    private val database: Database
) : EffectHandler<UpdateState> {
    override suspend fun handle(effect: UpdateState) {
        database.updateState(effect.path, effect.value)
    }
}
