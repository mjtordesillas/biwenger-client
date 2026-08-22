import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createBiwengerClient } from '../src/biwenger-client.js'

test('removeBid authenticates and DELETEs the offer endpoint, same path as reject/accept but no body', async () => {
  const calls = []
  const client = createBiwengerClient({
    baseUrl: 'https://example.test/api/v2',
    fetch: async (url, options = {}) => {
      calls.push({ url, options })
      if (url.endsWith('/auth/login')) return { ok: true, json: async () => ({ token: 'test-token' }) }
      if (url.endsWith('/account')) {
        return { ok: true, json: async () => ({ data: { leagues: [{ id: 7, user: { id: 42 } }], account: { credits: 0 } } }) }
      }
      return { ok: true }
    },
  })

  await client.removeBid({ email: 'test@example.com', password: 'secret', offerId: 4273101594 })

  assert.deepEqual(calls[2], {
    url: 'https://example.test/api/v2/offers/4273101594',
    options: {
      method: 'DELETE',
      headers: {
        Authorization: 'Bearer test-token',
        'X-League': '7',
        'X-User': '42',
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
    },
  })
})
