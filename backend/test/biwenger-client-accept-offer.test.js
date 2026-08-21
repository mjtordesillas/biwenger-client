import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createBiwengerClient } from '../src/biwenger-client.js'

test('acceptOffer authenticates and PUTs an accepted status to the offer endpoint', async () => {
  const calls = []
  const client = createBiwengerClient({
    baseUrl: 'https://example.test/api/v2',
    fetch: async (url, options = {}) => {
      calls.push({ url, options })
      if (url.endsWith('/auth/login')) return { ok: true, json: async () => ({ token: 'test-token' }) }
      if (url.endsWith('/account')) {
        return { ok: true, json: async () => ({ data: { leagues: [{ id: 7, user: { id: 42 } }], account: { credits: 0 } } }) }
      }
      return { ok: true, json: async () => ({ status: 200, data: { status: 'accepted' } }) }
    },
  })

  await client.acceptOffer({ email: 'test@example.com', password: 'secret', offerId: 3822815314 })

  assert.deepEqual(calls[2], {
    url: 'https://example.test/api/v2/offers/3822815314',
    options: {
      method: 'PUT',
      headers: {
        Authorization: 'Bearer test-token',
        'X-League': '7',
        'X-User': '42',
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ status: 'accepted' }),
    },
  })
})
