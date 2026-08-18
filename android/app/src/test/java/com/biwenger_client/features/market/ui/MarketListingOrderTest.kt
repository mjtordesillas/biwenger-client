package com.biwenger_client.features.market.ui

import com.biwenger_client.helpers.builders.aMarketListing
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MarketListingOrderTest {

    @Test
    fun `sorts by expiry ascending first`() {
        val soonest = aMarketListing(id = 1, until = 1000)
        val latest = aMarketListing(id = 2, until = 3000)
        val middle = aMarketListing(id = 3, until = 2000)

        val sorted = listOf(soonest, latest, middle).sortedWith(MarketListingOrder)

        assertThat(sorted.map { it.id }).containsExactly(1, 3, 2)
    }

    @Test
    fun `breaks an expiry tie by position descending`() {
        val goalkeeper = aMarketListing(id = 1, until = 1000, position = 1)
        val forward = aMarketListing(id = 2, until = 1000, position = 4)
        val midfielder = aMarketListing(id = 3, until = 1000, position = 3)

        val sorted = listOf(goalkeeper, forward, midfielder).sortedWith(MarketListingOrder)

        assertThat(sorted.map { it.id }).containsExactly(2, 3, 1)
    }

    @Test
    fun `breaks an expiry and position tie by market value descending`() {
        val cheap = aMarketListing(id = 1, until = 1000, position = 3, marketValue = 100000)
        val expensive = aMarketListing(id = 2, until = 1000, position = 3, marketValue = 300000)
        val mid = aMarketListing(id = 3, until = 1000, position = 3, marketValue = 200000)

        val sorted = listOf(cheap, expensive, mid).sortedWith(MarketListingOrder)

        assertThat(sorted.map { it.id }).containsExactly(2, 3, 1)
    }
}
