package com.biwenger_client.features.market.infrastructure

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.biwenger_client.infrastructure.network.Response

class HttpMarketServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: MarketService

    @Before
    fun beforeEach() {
        server = MockWebServer()
        server.start()
        service = HttpMarketService(baseUrl = server.url("/").toString(), apiKey = "test-key")
    }

    @After
    fun afterEach() {
        server.shutdown()
    }

    @Test
    fun `market parses the wrapped players array, including the market-specific fields`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"players":[{
                        "id":1,"name":"Brugué","position":4,"secondaryPosition":null,
                        "price":250000,"marketValue":280000,"priceIncrement":10000,"points":5,
                        "photoUrl":"https://cdn.biwenger.com/i/p/1.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/87.png",
                        "until":1787116441,"seller":"Rival FC"
                    }]}"""
                )
            )

            val result = service.market()

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val listings = (result as Response.Success).body
            assertThat(listings).hasSize(1)
            val listing = listings?.first()
            assertThat(listing?.name).isEqualTo("Brugué")
            assertThat(listing?.price).isEqualTo(250000)
            assertThat(listing?.marketValue).isEqualTo(280000)
            assertThat(listing?.until).isEqualTo(1787116441)
            assertThat(listing?.seller).isEqualTo("Rival FC")
        }
    }

    @Test
    fun `market defaults seller to null for a free-agent listing`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"players":[{
                        "id":1,"name":"Brugué","position":4,"secondaryPosition":null,
                        "price":250000,"marketValue":280000,"priceIncrement":10000,"points":5,
                        "photoUrl":"https://cdn.biwenger.com/i/p/1.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/87.png",
                        "until":1787116441,"seller":null
                    }]}"""
                )
            )

            val result = service.market()

            val listing = (result as Response.Success).body?.first()
            assertThat(listing?.seller).isNull()
        }
    }

    @Test
    fun `market sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"players":[]}"""))

            service.market()

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `market returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(403))

            val result = service.market()

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(403)
        }
    }
}
