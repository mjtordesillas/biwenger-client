import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createPlaceBidApiHandler } from '../src/place-bid-api-handler.js'

test('forwards the path player id and body amount with configured credentials and returns 200', async () => {
  let request
  const handler = createPlaceBidApiHandler({
    biwengerClient: { placeBid: async (value) => { request = value } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ pathParameters: { playerId: '24956' }, body: JSON.stringify({ amount: 150000 }) })

  assert.equal(response.statusCode, 200)
  assert.deepEqual(request, { email: 'test@example.com', password: 'secret', playerId: 24956, amount: 150000 })
})

test('returns a sanitized 502 for an invalid id, invalid amount, malformed body, or upstream failure', async () => {
  const handler = createPlaceBidApiHandler({
    biwengerClient: { placeBid: async () => { throw new Error('token=super-secret-leak') } },
  })

  const invalidId = await handler({ pathParameters: { playerId: 'nope' }, body: JSON.stringify({ amount: 150000 }) })
  const invalidAmount = await handler({ pathParameters: { playerId: '1' }, body: JSON.stringify({ amount: -5 }) })
  const malformedBody = await handler({ pathParameters: { playerId: '1' }, body: 'not-json' })
  const failed = await handler({ pathParameters: { playerId: '1' }, body: JSON.stringify({ amount: 150000 }) })

  assert.equal(invalidId.statusCode, 502)
  assert.equal(invalidAmount.statusCode, 502)
  assert.equal(malformedBody.statusCode, 502)
  assert.equal(failed.statusCode, 502)
  assert.doesNotMatch(failed.body, /super-secret-leak/)
})
