package com.biwenger_client.features.squad.infrastructure

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.biwenger_client.infrastructure.network.Response

class HttpPerformanceHistoryServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: PerformanceHistoryService

    @Before
    fun beforeEach() {
        server = MockWebServer()
        server.start()
        service = HttpPerformanceHistoryService(baseUrl = server.url("/").toString(), apiKey = "test-key")
    }

    @After
    fun afterEach() {
        server.shutdown()
    }

    @Test
    fun `performanceHistory parses the gameweeks`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"gameweeks":[{"matchDay":1,"points":4}]}"""
                )
            )

            val result = service.performanceHistory(42)

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val history = (result as Response.Success).body
            assertThat(history?.gameweeks).hasSize(1)
            assertThat(history?.gameweeks?.first()?.matchDay).isEqualTo(1)
            assertThat(history?.gameweeks?.first()?.points).isEqualTo(4)
        }
    }

    @Test
    fun `performanceHistory requests the player-scoped path`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"gameweeks":[]}"""))

            service.performanceHistory(42)

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/players/42/performance-history")
        }
    }

    @Test
    fun `performanceHistory sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"gameweeks":[]}"""))

            service.performanceHistory(42)

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `performanceHistory returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404))

            val result = service.performanceHistory(42)

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(404)
        }
    }
}
