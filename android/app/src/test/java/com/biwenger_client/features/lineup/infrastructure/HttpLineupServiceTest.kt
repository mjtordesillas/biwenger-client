package com.biwenger_client.features.lineup.infrastructure

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.biwenger_client.infrastructure.network.Response

class HttpLineupServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: LineupService

    @Before
    fun beforeEach() {
        server = MockWebServer()
        server.start()
        service = HttpLineupService(baseUrl = server.url("/").toString(), apiKey = "test-key")
    }

    @After
    fun afterEach() {
        server.shutdown()
    }

    @Test
    fun `lineup parses the formation and players, no wrapper`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"formation":"3-5-2","players":[{
                        "id":41101,"name":"Alfonso Herrero","position":1,"secondaryPosition":null,
                        "price":3880000,"priceIncrement":-30000,"points":0,
                        "photoUrl":"https://cdn.biwenger.com/i/p/41101.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/65.png"
                    }]}"""
                )
            )

            val result = service.lineup()

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val lineup = (result as Response.Success).body
            assertThat(lineup?.formation).isEqualTo("3-5-2")
            assertThat(lineup?.players).hasSize(1)
            assertThat(lineup?.players?.first()?.name).isEqualTo("Alfonso Herrero")
            assertThat(lineup?.players?.first()?.position).isEqualTo(1)
        }
    }

    @Test
    fun `lineup sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"formation":"4-4-2","players":[]}"""))

            service.lineup()

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `lineup returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(403))

            val result = service.lineup()

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(403)
        }
    }

    @Test
    fun `saveLineup PUTs {formation, playerIds} and parses the saved lineup back`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"formation":"3-5-2","players":[null]}"""))

            val result = service.saveLineup(formation = "3-5-2", playerIds = listOf(41101, null))

            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("PUT")
            assertThat(request.body.readUtf8()).isEqualTo("""{"formation":"3-5-2","playerIds":[41101,null]}""")

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val lineup = (result as Response.Success).body
            assertThat(lineup?.formation).isEqualTo("3-5-2")
            assertThat(lineup?.players).containsExactly(null)
        }
    }

    @Test
    fun `saveLineup sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"formation":"4-4-2","players":[]}"""))

            service.saveLineup(formation = "4-4-2", playerIds = emptyList())

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `saveLineup returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(400))

            val result = service.saveLineup(formation = "3-5-2", playerIds = emptyList())

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(400)
        }
    }
}
