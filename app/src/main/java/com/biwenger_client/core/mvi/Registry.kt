package com.biwenger_client.core.mvi

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.Event
import kotlin.reflect.KClass

interface Registry {
    fun dispatch(event: Event<*>)
    fun <T> registerEventHandler(name: String, handler: suspend (Event<T>) -> List<Effect>)
    fun <T> registerEventHandler(
        name: String,
        coeffects: List<Coeffect<*>>,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    )
    // For coeffects whose value depends on the event's own payload (e.g.
    // which player id to fetch price history for) — the coeffects list
    // can't be fixed at registration time like the overload above. See
    // docs/adrs/ADR-009-event-parameterized-coeffects.md.
    fun <T> registerEventHandler(
        name: String,
        coeffects: (Event<T>) -> List<Coeffect<*>>,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    )

    fun <T> removeEventHandler(name: String, handler: suspend (Event<T>) -> List<Effect>)
    fun <T> removeEventHandler(
        name: String,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    )
    fun <E : Effect> registerEffectHandler(effectClass: KClass<E>, handler: EffectHandler<E>)
    fun <C : Coeffect<T>, T> registerCoeffectHandler(
        coeffectClass: KClass<C>,
        handler: CoeffectHandler<C, T>
    )
}
