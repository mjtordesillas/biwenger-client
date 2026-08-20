package com.biwenger_client.features.lineup.domain.coeffects

import com.biwenger_client.features.lineup.infrastructure.LineupService
import com.biwenger_client.helpers.builders.aLineup
import com.biwenger_client.infrastructure.network.Response
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FetchLineupCoeffectHandlerTest {

    private val lineupService = mock<LineupService>()
    private val handler = FetchLineupCoeffectHandler(lineupService = lineupService)

    @Test
    fun `extract returns the lineup on success`() {
        runBlocking {
            val lineup = aLineup()
            whenever(lineupService.lineup()).thenReturn(Response.Success(lineup))

            val result = handler.extract(FetchLineupCoeffect)

            assertThat(result).isEqualTo(lineup)
        }
    }

    @Test
    fun `extract throws on error`() {
        runBlocking {
            whenever(lineupService.lineup()).thenReturn(Response.Error(502, "upstream_error"))

            assertThatThrownBy {
                runBlocking { handler.extract(FetchLineupCoeffect) }
            }.isInstanceOf(LineupFetchException::class.java)
        }
    }
}
