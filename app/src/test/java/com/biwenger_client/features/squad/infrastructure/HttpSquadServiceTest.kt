package com.biwenger_client.features.squad.infrastructure

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.biwenger_client.infrastructure.network.Response

class HttpSquadServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: SquadService

    @Before
    fun beforeEach() {
        server = MockWebServer()
        server.start()
        service = HttpSquadService(baseUrl = server.url("/").toString(), apiKey = "test-key")
    }

    @After
    fun afterEach() {
        server.shutdown()
    }

    @Test
    fun `squad parses the wrapped players array`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"players":[{
                        "id":1,"name":"Brugué","position":4,"secondaryPosition":null,
                        "price":280000,"priceIncrement":10000,"points":5,
                        "photoUrl":"https://cdn.biwenger.com/i/p/1.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/87.png"
                    }]}"""
                )
            )

            val result = service.squad()

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val players = (result as Response.Success).body
            assertThat(players).hasSize(1)
            val player = players?.first()
            assertThat(player?.name).isEqualTo("Brugué")
            assertThat(player?.priceIncrement).isEqualTo(10000)
            assertThat(player?.points).isEqualTo(5)
            assertThat(player?.photoUrl).isEqualTo("https://cdn.biwenger.com/i/p/1.png")
            assertThat(player?.teamCrestUrl).isEqualTo("https://cdn.biwenger.com/i/t/87.png")
        }
    }

    @Test
    fun `squad sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"players":[]}"""))

            service.squad()

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `squad returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(403))

            val result = service.squad()

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(403)
        }
    }
}
