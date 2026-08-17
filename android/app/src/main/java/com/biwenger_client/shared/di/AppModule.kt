package com.biwenger_client.shared.di

import com.biwenger_client.BuildConfig
import com.biwenger_client.core.mvi.AppStore
import com.biwenger_client.core.mvi.ChannelRegistry
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.core.mvi.Store
import com.biwenger_client.core.navigation.NavigationProvider
import com.biwenger_client.core.navigation.Navigator
import com.biwenger_client.core.state.Database
import com.biwenger_client.features.market.MarketStateInitializer
import com.biwenger_client.features.market.infrastructure.HttpMarketService
import com.biwenger_client.features.market.infrastructure.MarketService
import com.biwenger_client.features.squad.SquadStateInitializer
import com.biwenger_client.features.squad.infrastructure.HttpMatchDayDetailsService
import com.biwenger_client.features.squad.infrastructure.HttpPerformanceHistoryService
import com.biwenger_client.features.squad.infrastructure.HttpPriceHistoryService
import com.biwenger_client.features.squad.infrastructure.HttpSquadService
import com.biwenger_client.features.squad.infrastructure.MatchDayDetailsService
import com.biwenger_client.features.squad.infrastructure.PerformanceHistoryService
import com.biwenger_client.features.squad.infrastructure.PriceHistoryService
import com.biwenger_client.features.squad.infrastructure.SquadService
import com.biwenger_client.shared.CoeffectsHandlerRegistration
import com.biwenger_client.shared.EffectsHandlerRegistration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

private const val BASE_URL = "https://dgf40str28.execute-api.eu-west-1.amazonaws.com/production/"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    @Provides
    @Singleton
    fun provideRegistry(@ApplicationScope scope: CoroutineScope): Registry {
        return ChannelRegistry(scope)
    }

    @Provides
    @Singleton
    fun provideDatabase(): Database {
        val state = SquadStateInitializer().initialState() + MarketStateInitializer().initialState()
        return Database(initialState = state)
    }

    @Provides
    @Singleton
    fun provideNavigationProvider(): NavigationProvider {
        return NavigationProvider()
    }

    @Provides
    @Singleton
    fun provideNavigator(navigationProvider: NavigationProvider): Navigator {
        return navigationProvider
    }

    @Provides
    @Singleton
    fun provideStore(registry: Registry, database: Database): Store {
        return AppStore(registry = registry, database = database)
    }

    @Provides
    @Singleton
    fun provideEffectsHandlerRegistration(
        registry: Registry,
        database: Database,
        navigator: Navigator,
    ): EffectsHandlerRegistration {
        return EffectsHandlerRegistration(registry = registry, database = database, navigator = navigator)
    }

    @Provides
    @Singleton
    fun provideCoeffectsHandlerRegistration(
        registry: Registry,
        squadService: SquadService,
        priceHistoryService: PriceHistoryService,
        performanceHistoryService: PerformanceHistoryService,
        matchDayDetailsService: MatchDayDetailsService,
        marketService: MarketService,
    ): CoeffectsHandlerRegistration {
        return CoeffectsHandlerRegistration(
            registry = registry,
            squadService = squadService,
            priceHistoryService = priceHistoryService,
            performanceHistoryService = performanceHistoryService,
            matchDayDetailsService = matchDayDetailsService,
            marketService = marketService,
        )
    }

    @Provides
    @Singleton
    fun provideSquadService(): SquadService {
        return HttpSquadService(baseUrl = BASE_URL, apiKey = BuildConfig.API_KEY)
    }

    @Provides
    @Singleton
    fun providePriceHistoryService(): PriceHistoryService {
        return HttpPriceHistoryService(baseUrl = BASE_URL, apiKey = BuildConfig.API_KEY)
    }

    @Provides
    @Singleton
    fun providePerformanceHistoryService(): PerformanceHistoryService {
        return HttpPerformanceHistoryService(baseUrl = BASE_URL, apiKey = BuildConfig.API_KEY)
    }

    @Provides
    @Singleton
    fun provideMatchDayDetailsService(): MatchDayDetailsService {
        return HttpMatchDayDetailsService(baseUrl = BASE_URL, apiKey = BuildConfig.API_KEY)
    }

    @Provides
    @Singleton
    fun provideMarketService(): MarketService {
        return HttpMarketService(baseUrl = BASE_URL, apiKey = BuildConfig.API_KEY)
    }
}
