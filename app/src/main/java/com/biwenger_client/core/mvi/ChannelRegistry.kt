package com.biwenger_client.core.mvi

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectFailure
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.events.event
import com.biwenger_client.core.state.Loadable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.collections.set
import kotlin.reflect.KClass

class ChannelRegistry(
    private val scope: CoroutineScope
) : Registry {
    private val events = Channel<Event<*>>(Channel.UNLIMITED)
    private val effectHandlers = mutableMapOf<KClass<out Effect>, EffectHandler<Effect>>()
    private val coeffectHandlers = mutableMapOf<KClass<out Coeffect<*>>, CoeffectHandler<Coeffect<*>, *>>()
    private val eventHandlers = mutableMapOf<String, MutableList<suspend (Event<*>) -> List<Effect>>>()
    private val handlerWrappers = mutableMapOf<Pair<String, Any>, suspend (Event<*>) -> List<Effect>>()

    init {
        scope.launch {
            events.consumeAsFlow()
                .collect { event ->
                    val effects = handleEvent(event)
                    effects.forEach { effect ->
                        handleEffect(effect)
                    }
                }
        }
    }

    override fun dispatch(event: Event<*>) {
        events.trySend(event)
    }

    override fun <T> registerEventHandler(
        name: String,
        handler: suspend (Event<T>) -> List<Effect>
    ) {
        val wrapper: suspend (Event<*>) -> List<Effect> = { event -> handler(event as Event<T>) }
        handlerWrappers[Pair(name, handler)] = wrapper
        eventHandlers.getOrPut(key = name) { mutableListOf() }.add(wrapper)
    }

    override fun <T> registerEventHandler(
        name: String,
        coeffects: List<Coeffect<*>>,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    ) {
        val wrapper: suspend (Event<*>) -> List<Effect> = { event ->
            val resolvedValues = coeffects.associateWith { resolveCoeffect(it) }
            handler(event as Event<T>, Coeffects(values = resolvedValues as Map<Coeffect<*>, Any?>))
        }
        handlerWrappers[Pair(name, handler)] = wrapper
        eventHandlers.getOrPut(key = name) { mutableListOf() }.add(wrapper)
    }

    override fun <T> removeEventHandler(
        name: String,
        handler: suspend (Event<T>) -> List<Effect>
    ) {
        val wrapper = handlerWrappers.remove(Pair(name, handler)) ?: return
        eventHandlers[name]?.remove(wrapper)
    }

    override fun <T> removeEventHandler(
        name: String,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    ) {
        val wrapper = handlerWrappers.remove(Pair(name, handler)) ?: return
        eventHandlers[name]?.remove(wrapper)
    }

    override fun <E : Effect> registerEffectHandler(
        effectClass: KClass<E>,
        handler: EffectHandler<E>
    ) {
        @Suppress("UNCHECKED_CAST")
        effectHandlers[effectClass] = handler as EffectHandler<Effect>
    }

    override fun <C : Coeffect<T>, T> registerCoeffectHandler(
        coeffectClass: KClass<C>,
        handler: CoeffectHandler<C, T>
    ) {
        @Suppress("UNCHECKED_CAST")
        coeffectHandlers[coeffectClass as KClass<out Coeffect<*>>] = handler as CoeffectHandler<Coeffect<*>, *>
    }

    private suspend fun <T> resolveCoeffect(coeffect: Coeffect<T>): Loadable<T> {
        val matchingHandler = coeffectHandlers.entries.find { (registeredClass, _) ->
            registeredClass.java.isAssignableFrom(coeffect::class.java)
        }?.value

        if (matchingHandler == null) {
            throw IllegalStateException("No coeffect handler registered for ${coeffect::class.java.name}")
        }

        @Suppress("UNCHECKED_CAST")
        val handler = matchingHandler as CoeffectHandler<Coeffect<T>, T>
        return try {
            Loadable.Success(value = handler.extract(coeffect))
        } catch (error: Throwable) {
            val failed = handler.onFailure(coeffect = coeffect, error = error)
            dispatch(event = event(
                name = "coeffect.failed",
                payload = CoeffectFailure(
                    coeffectName = coeffect::class.java.name,
                    error = error
                )
            ))
            failed
        }
    }

    suspend fun handleEvent(event: Event<*>): List<Effect> {
        val handlers = eventHandlers[event.name]?.toList() ?: return emptyList()
        val allEffects = mutableListOf<Effect>()
        for (handler in handlers) {
            allEffects.addAll(handler(event))
        }
        return allEffects
    }

    suspend fun handleEffect(effect: Effect) {
        val matchingHandler = effectHandlers.entries.find { (registeredClass, _) ->
            registeredClass.java.isAssignableFrom(effect::class.java)
        }?.value

        matchingHandler?.handle(effect) ?: run {
        }
    }
}
