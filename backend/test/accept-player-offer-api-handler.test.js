import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createAcceptPlayerOfferApiHandler } from '../src/accept-player-offer-api-handler.js'

test('forwards the path offer id with configured credentials and returns 200', async () => {
  let request
  const handler = createAcceptPlayerOfferApiHandler({
    biwengerClient: { acceptOffer: async (value) => { request = value } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ pathParameters: { offerId: '3822815314' } })

  assert.equal(response.statusCode, 200)
  assert.deepEqual(request, { email: 'test@example.com', password: 'secret', offerId: 3822815314 })
})

test('returns a sanitized 502 for an invalid id or upstream failure', async () => {
  const handler = createAcceptPlayerOfferApiHandler({
    biwengerClient: { acceptOffer: async () => { throw new Error('token=super-secret-leak') } },
  })

  const invalid = await handler({ pathParameters: { offerId: 'nope' } })
  const failed = await handler({ pathParameters: { offerId: '1' } })

  assert.equal(invalid.statusCode, 502)
  assert.equal(failed.statusCode, 502)
  assert.doesNotMatch(failed.body, /super-secret-leak/)
})
