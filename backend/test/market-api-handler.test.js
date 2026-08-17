import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createMarketApiHandler } from '../src/market-api-handler.js'

const fakeBiwengerClient = (listings) => ({
  getCurrentMarket: async () => listings,
})

test('returns a 200 JSON body with the market listings, shaped via toMarketListingView, on success', async () => {
  const handler = createMarketApiHandler({
    biwengerClient: fakeBiwengerClient([
      {
        sale: { price: 250000, until: 1787116441, user: { id: 99, name: 'Rival FC' } },
        player: { id: 1, name: 'Brugué', teamID: 87, position: 4, price: 280000, priceIncrement: 10000, points: 5 },
      },
    ]),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  assert.deepEqual(JSON.parse(response.body), {
    players: [{
      id: 1,
      name: 'Brugué',
      position: 4,
      secondaryPosition: null,
      price: 250000,
      priceIncrement: 10000,
      points: 5,
      photoUrl: 'https://cdn.biwenger.com/i/p/1.png',
      teamCrestUrl: 'https://cdn.biwenger.com/i/t/87.png',
      marketValue: 280000,
      until: 1787116441,
      seller: 'Rival FC',
    }],
  })
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createMarketApiHandler({
    biwengerClient: { getCurrentMarket: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
