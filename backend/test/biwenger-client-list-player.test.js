import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createBiwengerClient } from '../src/biwenger-client.js'

test('listPlayer authenticates and POSTs a sell listing to the market endpoint', async () => {
  const calls = []
  const client = createBiwengerClient({
    baseUrl: 'https://example.test/api/v2',
    fetch: async (url, options = {}) => {
      calls.push({ url, options })
      if (url.endsWith('/auth/login')) return { ok: true, json: async () => ({ token: 'test-token' }) }
      if (url.endsWith('/account')) {
        return { ok: true, json: async () => ({ data: { leagues: [{ id: 7, user: { id: 42 } }], account: { credits: 0 } } }) }
      }
      return { ok: true, json: async () => ({ status: 200 }) }
    },
  })

  await client.listPlayer({ email: 'test@example.com', password: 'secret', playerId: 15396, price: 35_000_000 })

  assert.deepEqual(calls[2], {
    url: 'https://example.test/api/v2/market',
    options: {
      method: 'POST',
      headers: {
        Authorization: 'Bearer test-token',
        'X-League': '7',
        'X-User': '42',
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ type: 'sell', player: 15396, price: 35_000_000 }),
    },
  })
})
