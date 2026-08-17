package com.biwenger_client

import android.app.Application
import com.biwenger_client.shared.CoeffectsHandlerRegistration
import com.biwenger_client.shared.EffectsHandlerRegistration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BiwengerClientApplication : Application() {
    @Inject
    lateinit var effectsHandlerRegistration: EffectsHandlerRegistration
    @Inject
    lateinit var coeffectsHandlerRegistration: CoeffectsHandlerRegistration

    override fun onCreate() {
        super.onCreate()
        effectsHandlerRegistration.register()
        coeffectsHandlerRegistration.register()
    }
}
