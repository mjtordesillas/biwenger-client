import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createSquadApiHandler } from '../src/squad-api-handler.js'

const fakeBiwengerClient = (players) => ({
  getMySquad: async () => players,
})

test('returns a 200 JSON body with the squad, shaped via toSquadPlayerView, on success', async () => {
  const handler = createSquadApiHandler({
    biwengerClient: fakeBiwengerClient([
      {
        player: { id: 1, name: 'Brugué', teamID: 87, position: 4, price: 280000, priceIncrement: 10000, points: 5, status: 'ok' },
        owner: { date: 1786573790 },
        inMarket: false,
        offerAmount: null,
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
      signedAt: 1786573790,
      signedPrice: null,
      lockedUntil: null,
      inMarket: false,
      offerAmount: null,
      status: 'ok',
    }],
  })
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createSquadApiHandler({
    biwengerClient: { getMySquad: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
