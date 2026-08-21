import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createListPlayerApiHandler } from '../src/list-player-api-handler.js'

test('forwards the path player id with the fixed price and configured credentials, returns 200', async () => {
  let request
  const handler = createListPlayerApiHandler({
    biwengerClient: { listPlayer: async (value) => { request = value } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ pathParameters: { playerId: '15396' } })

  assert.equal(response.statusCode, 200)
  assert.deepEqual(request, { email: 'test@example.com', password: 'secret', playerId: 15396, price: 35_000_000 })
})

test('returns a sanitized 502 for an invalid id or upstream failure', async () => {
  const handler = createListPlayerApiHandler({
    biwengerClient: { listPlayer: async () => { throw new Error('token=super-secret-leak') } },
  })

  const invalid = await handler({ pathParameters: { playerId: 'nope' } })
  const failed = await handler({ pathParameters: { playerId: '1' } })

  assert.equal(invalid.statusCode, 502)
  assert.equal(failed.statusCode, 502)
  assert.doesNotMatch(failed.body, /super-secret-leak/)
})
