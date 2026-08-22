import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createPlayerBidsApiHandler } from '../src/player-bids-api-handler.js'

const fakeBiwengerClient = (bids) => ({
  getMyBidsOnOtherPlayers: async () => bids,
})

test('returns a 200 JSON body with my outgoing bids, shaped via toPlayerBidView, on success', async () => {
  const handler = createPlayerBidsApiHandler({
    biwengerClient: fakeBiwengerClient([
      {
        offer: { id: 4273101594, amount: 150000, until: 1787461200, from: { id: 42, name: 'Me' }, to: null, requestedPlayers: [1], status: 'waiting', type: 'purchase' },
        sale: { date: 1787288871, until: 1787461200, price: 150000, player: { id: 1 }, user: null },
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
      offerId: 4273101594,
      id: 1,
      name: 'Brugué',
      position: 4,
      secondaryPosition: null,
      price: 150000,
      priceIncrement: 10000,
      points: 5,
      photoUrl: 'https://cdn.biwenger.com/i/p/1.png',
      teamCrestUrl: 'https://cdn.biwenger.com/i/t/87.png',
      marketValue: 280000,
      until: 1787461200,
      seller: null,
      amount: 150000,
    }],
  })
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createPlayerBidsApiHandler({
    biwengerClient: { getMyBidsOnOtherPlayers: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
