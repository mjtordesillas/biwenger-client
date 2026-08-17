package com.biwenger_client.core.effects

interface EffectHandler<T : Effect> {
    suspend fun handle(effect: T)
}
