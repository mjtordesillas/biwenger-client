import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createUnlistPlayerApiHandler } from '../src/unlist-player-api-handler.js'

test('forwards the path player id with configured credentials and returns 200', async () => {
  let request
  const handler = createUnlistPlayerApiHandler({
    biwengerClient: { unlistPlayer: async (value) => { request = value } },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler({ pathParameters: { playerId: '37817' } })

  assert.equal(response.statusCode, 200)
  assert.deepEqual(request, { email: 'test@example.com', password: 'secret', playerId: 37817 })
})

test('returns a sanitized 502 for an invalid id or upstream failure', async () => {
  const handler = createUnlistPlayerApiHandler({
    biwengerClient: { unlistPlayer: async () => { throw new Error('token=super-secret-leak') } },
  })

  const invalid = await handler({ pathParameters: { playerId: 'nope' } })
  const failed = await handler({ pathParameters: { playerId: '1' } })

  assert.equal(invalid.statusCode, 502)
  assert.equal(failed.statusCode, 502)
  assert.doesNotMatch(failed.body, /super-secret-leak/)
})
