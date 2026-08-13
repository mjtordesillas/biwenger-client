import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createSquadApiHandler } from '../src/squad-api-handler.js'

const fakeBiwengerClient = (players) => ({
  getMySquad: async () => players,
})

test('returns a 200 JSON body with the squad on success', async () => {
  const handler = createSquadApiHandler({
    biwengerClient: fakeBiwengerClient([{ id: 1, name: 'Brugué', position: 4, price: 280000 }]),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  assert.deepEqual(JSON.parse(response.body), {
    players: [{ id: 1, name: 'Brugué', position: 4, price: 280000 }],
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
