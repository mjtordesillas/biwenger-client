import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createSquadHandler } from '../src/squad-handler.js'

const fakeBiwengerClient = (players) => ({
  getMySquad: async () => players,
})

test('returns a 200 HTML page listing the squad on success', async () => {
  const handler = createSquadHandler({
    biwengerClient: fakeBiwengerClient([{ id: 1, name: 'Brugué', position: 4, price: 280000 }]),
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'text/html; charset=utf-8')
  assert.match(response.body, /Brugué/)
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createSquadHandler({
    biwengerClient: { getMySquad: async () => { throw new Error('token=super-secret-leak') } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
