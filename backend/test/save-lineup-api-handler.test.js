import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createSaveLineupApiHandler } from '../src/save-lineup-api-handler.js'

const fakeBiwengerClient = (lineup) => ({
  saveLineup: async () => lineup,
})

test('returns a 200 JSON body with the saved lineup, shaped via toLineupView, on success', async () => {
  const handler = createSaveLineupApiHandler({
    biwengerClient: fakeBiwengerClient({
      formation: '3-5-2',
      players: [
        { id: 41101, name: 'Alfonso Herrero', teamID: 65, position: 1, price: 3880000, priceIncrement: -30000, points: 0 },
        null,
      ],
    }),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ body: JSON.stringify({ formation: '3-5-2', playerIds: [41101, null] }) })

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  const body = JSON.parse(response.body)
  assert.equal(body.formation, '3-5-2')
  assert.equal(body.players.length, 2)
  assert.equal(body.players[0].id, 41101)
  assert.equal(body.players[1], null)
})

test('forwards the parsed body as {email, password, formation, playerIds} to the client', async () => {
  let capturedRequest
  const handler = createSaveLineupApiHandler({
    biwengerClient: {
      saveLineup: async (request) => {
        capturedRequest = request
        return { formation: '4-4-2', players: [] }
      },
    },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  await handler({ body: JSON.stringify({ formation: '4-4-2', playerIds: [1, 2, null] }) })

  assert.deepEqual(capturedRequest, {
    email: 'test@example.com',
    password: 'secret',
    formation: '4-4-2',
    playerIds: [1, 2, null],
  })
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createSaveLineupApiHandler({
    biwengerClient: { saveLineup: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ body: JSON.stringify({ formation: '3-5-2', playerIds: [] }) })

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})

test('returns a 502 with no upstream details when the request body is malformed', async () => {
  const handler = createSaveLineupApiHandler({
    biwengerClient: fakeBiwengerClient({ formation: '3-5-2', players: [] }),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ body: 'not-json' })

  assert.equal(response.statusCode, 502)
})
