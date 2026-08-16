package com.biwenger_client.features.squad.infrastructure

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.biwenger_client.features.squad.domain.models.SubstitutionEvent
import com.biwenger_client.infrastructure.network.Response

class HttpMatchDayDetailsServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: MatchDayDetailsService

    @Before
    fun beforeEach() {
        server = MockWebServer()
        server.start()
        service = HttpMatchDayDetailsService(baseUrl = server.url("/").toString(), apiKey = "test-key")
    }

    @After
    fun afterEach() {
        server.shutdown()
    }

    @Test
    fun `matchDayDetails parses the header, both score breakdowns, the media total, and substitutions`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{
                        "matchDay":8,
                        "kickoff":1741604400,
                        "home":{"id":87,"name":"Betis","score":2,"crestUrl":"https://cdn.biwenger.com/i/t/87.png"},
                        "away":{"id":91,"name":"Alavés","score":1,"crestUrl":"https://cdn.biwenger.com/i/t/91.png"},
                        "as":{"points":9,"rows":[{"type":"picas","count":2,"points":6},{"type":"goal","count":1,"points":3}]},
                        "sofaScore":{"points":11,"rows":[{"type":"sofascore","rating":8.4,"points":11}]},
                        "media":10,
                        "substitutions":[{"type":"substitutedOff","minute":70}]
                    }""".trimIndent()
                )
            )

            val result = service.matchDayDetails(42, 8, "current")

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val details = (result as Response.Success).body
            assertThat(details?.matchDay).isEqualTo(8)
            assertThat(details?.kickoff).isEqualTo(1741604400)
            assertThat(details?.home?.name).isEqualTo("Betis")
            assertThat(details?.away?.score).isEqualTo(1)
            assertThat(details?.diarioAs?.points).isEqualTo(9)
            assertThat(details?.diarioAs?.rows).hasSize(2)
            assertThat(details?.sofaScore?.points).isEqualTo(11)
            assertThat(details?.media).isEqualTo(10)
            assertThat(details?.substitutions).containsExactly(SubstitutionEvent(type = "substitutedOff", minute = 70))
        }
    }

    @Test
    fun `matchDayDetails requests the player-scoped path with the matchDay and season query params`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404))

            service.matchDayDetails(42, 8, "previous")

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/players/42/match-day-details?matchDay=8&season=previous")
        }
    }

    @Test
    fun `matchDayDetails sends the api key header`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404))

            service.matchDayDetails(42, 8, "current")

            val request = server.takeRequest()
            assertThat(request.getHeader("x-api-key")).isEqualTo("test-key")
        }
    }

    @Test
    fun `matchDayDetails returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404))

            val result = service.matchDayDetails(42, 8, "current")

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(404)
        }
    }
}
