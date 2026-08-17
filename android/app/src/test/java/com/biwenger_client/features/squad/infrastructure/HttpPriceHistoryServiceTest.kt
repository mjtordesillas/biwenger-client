package com.biwenger_client.features.squad.infrastructure

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.biwenger_client.infrastructure.network.Response

class HttpPriceHistoryServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: PriceHistoryService

    @Before
    fun beforeEach() {
        server = MockWebServer()
        server.start()
        service = HttpPriceHistoryService(baseUrl = server.url("/").toString(), apiKey = "test-key")
    }

    @After
    fun afterEach() {
        server.shutdown()
    }

    @Test
    fun `priceHistory parses the seasonStart and prices`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"seasonStart":"2026-07-01","prices":[{"date":"2026-07-01","price":5500000}]}"""
                )
            )

            val result = service.priceHistory(42)

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val history = (result as Response.Success).body
            assertThat(history?.seasonStart).isEqualTo("2026-07-01")
            assertThat(history?.prices).hasSize(1)
            assertThat(history?.prices?.first()?.price).isEqualTo(5500000)
        }
    }

    @Test
    fun `priceHistory requests the player-scoped path`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"seasonStart":"2026-07-01","prices":[]}"""))

            service.priceHistory(42)

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/players/42/price-history")
        }
    }

    @Test
    fun `priceHistory sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"seasonStart":"2026-07-01","prices":[]}"""))

            service.priceHistory(42)

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `priceHistory returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404))

            val result = service.priceHistory(42)

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(404)
        }
    }
}
