package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.features.squad.infrastructure.MatchDayDetailsService
import com.biwenger_client.helpers.builders.aMatchDayDetails
import com.biwenger_client.infrastructure.network.Response
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FetchMatchDayDetailsCoeffectHandlerTest {

    private val matchDayDetailsService = mock<MatchDayDetailsService>()
    private val handler = FetchMatchDayDetailsCoeffectHandler(matchDayDetailsService = matchDayDetailsService)

    @Test
    fun `extract returns the match day details for the coeffect's player id, match day, and season on success`() {
        runBlocking {
            val details = aMatchDayDetails()
            whenever(matchDayDetailsService.matchDayDetails(42, 8, "previous")).thenReturn(Response.Success(details))

            val result = handler.extract(FetchMatchDayDetailsCoeffect(playerId = 42, matchDay = 8, season = "previous"))

            assertThat(result).isEqualTo(details)
        }
    }

    @Test
    fun `extract throws on error`() {
        runBlocking {
            whenever(matchDayDetailsService.matchDayDetails(42, 8, "current")).thenReturn(Response.Error(502, "upstream_error"))

            assertThatThrownBy {
                runBlocking { handler.extract(FetchMatchDayDetailsCoeffect(playerId = 42, matchDay = 8, season = "current")) }
            }.isInstanceOf(MatchDayDetailsFetchException::class.java)
        }
    }
}
