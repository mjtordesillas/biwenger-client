import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createLineupApiHandler } from '../src/lineup-api-handler.js'

const fakeBiwengerClient = (lineup) => ({
  getLineup: async () => lineup,
})

test('returns a 200 JSON body with the lineup, shaped via toLineupView, on success', async () => {
  const handler = createLineupApiHandler({
    biwengerClient: fakeBiwengerClient({
      formation: '3-5-2',
      players: [
        { id: 41101, name: 'Alfonso Herrero', teamID: 65, position: 1, price: 3880000, priceIncrement: -30000, points: 0 },
      ],
    }),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  assert.deepEqual(JSON.parse(response.body), {
    formation: '3-5-2',
    players: [{
      id: 41101,
      name: 'Alfonso Herrero',
      position: 1,
      secondaryPosition: null,
      price: 3880000,
      priceIncrement: -30000,
      points: 0,
      photoUrl: 'https://cdn.biwenger.com/i/p/41101.png',
      teamCrestUrl: 'https://cdn.biwenger.com/i/t/65.png',
    }],
  })
})

test('keeps a vacant slot (null) in the response body, at its index', async () => {
  const handler = createLineupApiHandler({
    biwengerClient: fakeBiwengerClient({
      formation: '3-5-2',
      players: [
        { id: 41101, name: 'Alfonso Herrero', teamID: 65, position: 1, price: 3880000, priceIncrement: -30000, points: 0 },
        null,
      ],
    }),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  const body = JSON.parse(response.body)
  assert.equal(body.players.length, 2)
  assert.equal(body.players[1], null)
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createLineupApiHandler({
    biwengerClient: { getLineup: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
