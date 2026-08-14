package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.features.squad.domain.models.Player
import com.biwenger_client.features.squad.infrastructure.SquadService
import com.biwenger_client.infrastructure.network.Response
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FetchSquadCoeffectHandlerTest {

    private val squadService = mock<SquadService>()
    private val handler = FetchSquadCoeffectHandler(squadService = squadService)

    @Test
    fun `extract returns the squad on success`() {
        runBlocking {
            val players = listOf(Player(id = 1, name = "Brugué", position = 4, price = 280000))
            whenever(squadService.squad()).thenReturn(Response.Success(players))

            val result = handler.extract(FetchSquadCoeffect)

            assertThat(result).isEqualTo(players)
        }
    }

    @Test
    fun `extract throws on error`() {
        runBlocking {
            whenever(squadService.squad()).thenReturn(Response.Error(502, "upstream_error"))

            assertThatThrownBy {
                runBlocking { handler.extract(FetchSquadCoeffect) }
            }.isInstanceOf(SquadFetchException::class.java)
        }
    }
}
