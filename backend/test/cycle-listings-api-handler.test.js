import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createCycleListingsApiHandler } from '../src/cycle-listings-api-handler.js'

test('forwards configured credentials and returns the unlisted/listed summary with 200', async () => {
  let request
  const handler = createCycleListingsApiHandler({
    biwengerClient: {
      cycleListings: async (value) => {
        request = value
        return { unlisted: [10], listed: [30, 20] }
      },
    },
    credentials: { email: 'test@example.com', password: 'secret' },
  })

  const response = await handler()

  assert.equal(response.statusCode, 200)
  assert.deepEqual(request, { email: 'test@example.com', password: 'secret' })
  assert.deepEqual(JSON.parse(response.body), { unlisted: [10], listed: [30, 20] })
})

test('returns a sanitized 502 on upstream failure', async () => {
  const handler = createCycleListingsApiHandler({
    biwengerClient: { cycleListings: async () => { throw new Error('token=super-secret-leak') } },
  })

  const response = await handler()

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
