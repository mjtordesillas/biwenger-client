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

    @Test
    fun `myListings parses the wrapped players array, same shape as market`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"players":[{
                        "id":1,"name":"Brugué","position":4,"secondaryPosition":null,
                        "price":250000,"marketValue":280000,"priceIncrement":10000,"points":5,
                        "photoUrl":"https://cdn.biwenger.com/i/p/1.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/87.png",
                        "until":1787116441,"seller":"My Team"
                    }]}"""
                )
            )

            val result = service.myListings()

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val listings = (result as Response.Success).body
            assertThat(listings).hasSize(1)
            val listing = listings?.first()
            assertThat(listing?.name).isEqualTo("Brugué")
            assertThat(listing?.price).isEqualTo(250000)
            assertThat(listing?.seller).isEqualTo("My Team")
        }
    }

    @Test
    fun `myListings requests market_my-listings`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"players":[]}"""))

            service.myListings()

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/market/my-listings")
        }
    }

    @Test
    fun `myListings returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(403))

            val result = service.myListings()

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(403)
        }
    }

    @Test
    fun `offers parses the wrapped players array, including the offer-specific fields`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"players":[{
                        "offerId":99,"id":1,"name":"Brugué","position":4,"secondaryPosition":null,
                        "price":280000,"priceIncrement":10000,"points":5,
                        "photoUrl":"https://cdn.biwenger.com/i/p/1.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/87.png",
                        "amount":300000,"until":1787115600,"bidder":null
                    }]}"""
                )
            )

            val result = service.offers()

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val offers = (result as Response.Success).body
            assertThat(offers).hasSize(1)
            val offer = offers?.first()
            assertThat(offer?.name).isEqualTo("Brugué")
            assertThat(offer?.offerId).isEqualTo(99)
            assertThat(offer?.price).isEqualTo(280000)
            assertThat(offer?.amount).isEqualTo(300000)
            assertThat(offer?.bidder).isNull()
        }
    }

    @Test
    fun `offers requests market_offers`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"players":[]}"""))

            service.offers()

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/market/offers")
        }
    }

    @Test
    fun `offers returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(403))

            val result = service.offers()

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(403)
        }
    }

    @Test
    fun `rejectOffer PUTs rejected status to the offer endpoint`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":200}"""))

            val result = service.rejectOffer(3822815314)

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("PUT")
            assertThat(request.path).isEqualTo("/market/offers/3822815314")
            assertThat(request.body.readUtf8()).isEqualTo("""{"status":"rejected"}""")
        }
    }

    @Test
    fun `acceptOffer PUTs accepted status to the offer's accept endpoint`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":200}"""))

            val result = service.acceptOffer(3822815314)

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("PUT")
            assertThat(request.path).isEqualTo("/market/offers/3822815314/accept")
            assertThat(request.body.readUtf8()).isEqualTo("""{"status":"accepted"}""")
        }
    }

    @Test
    fun `unlistPlayer DELETEs the my-listings endpoint, no body`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":200}"""))

            val result = service.unlistPlayer(37817)

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("DELETE")
            assertThat(request.path).isEqualTo("/market/my-listings/37817")
            assertThat(request.body.size).isEqualTo(0)
        }
    }

    @Test
    fun `listPlayer POSTs the my-listings endpoint, no body`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"status":200}"""))

            val result = service.listPlayer(15396)

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val request = server.takeRequest()
            assertThat(request.method).isEqualTo("POST")
            assertThat(request.path).isEqualTo("/market/my-listings/15396")
            assertThat(request.body.size).isEqualTo(0)
        }
    }

    @Test
    fun `bids parses the wrapped players array, including the bid-specific fields`() {
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """{"players":[{
                        "id":1,"name":"Brugué","position":4,"secondaryPosition":null,
                        "price":150000,"marketValue":200000,"priceIncrement":-5000,"points":2,
                        "photoUrl":"https://cdn.biwenger.com/i/p/1.png",
                        "teamCrestUrl":"https://cdn.biwenger.com/i/t/87.png",
                        "until":1787461200,"seller":null,"amount":150000
                    }]}"""
                )
            )

            val result = service.bids()

            assertThat(result).isInstanceOf(Response.Success::class.java)
            val bids = (result as Response.Success).body
            assertThat(bids).hasSize(1)
            val bid = bids?.first()
            assertThat(bid?.name).isEqualTo("Brugué")
            assertThat(bid?.price).isEqualTo(150000)
            assertThat(bid?.marketValue).isEqualTo(200000)
            assertThat(bid?.amount).isEqualTo(150000)
            assertThat(bid?.seller).isNull()
        }
    }

    @Test
    fun `bids requests market_my-bids`() {
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"players":[]}"""))

            service.bids()

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/market/my-bids")
        }
    }

    @Test
    fun `bids returns an Error on a non-2xx response`() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(403))

            val result = service.bids()

            assertThat(result).isInstanceOf(Response.Error::class.java)
            assertThat((result as Response.Error).code).isEqualTo(403)
        }
    }
}
