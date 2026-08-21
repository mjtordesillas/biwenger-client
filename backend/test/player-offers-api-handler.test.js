import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createPlayerOffersApiHandler } from '../src/player-offers-api-handler.js'

const fakeBiwengerClient = (offers) => ({
  getOffersOnMyPlayers: async () => offers,
})

test('returns a 200 JSON body with the offers on my players, shaped via toPlayerOfferView, on success', async () => {
  const handler = createPlayerOffersApiHandler({
    biwengerClient: fakeBiwengerClient([
      {
        offer: { amount: 300000, from: null, to: { id: 42 }, requestedPlayers: [1], status: 'waiting', type: 'purchase' },
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
      price: 280000,
      priceIncrement: 10000,
      points: 5,
      photoUrl: 'https://cdn.biwenger.com/i/p/1.png',
      teamCrestUrl: 'https://cdn.biwenger.com/i/t/87.png',
      amount: 300000,
      bidder: null,
    }],
  })
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createPlayerOffersApiHandler({
    biwengerClient: { getOffersOnMyPlayers: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
