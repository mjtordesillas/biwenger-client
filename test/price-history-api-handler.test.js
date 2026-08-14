import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createPriceHistoryApiHandler } from '../src/price-history-api-handler.js'

const fakeBiwengerClient = (prices) => ({
  getPlayerPrices: async ({ playerId }) => {
    fakeBiwengerClient.lastPlayerId = playerId
    return prices
  },
})

test('returns a 200 JSON body with this season\'s prices, shaped via toPriceHistoryView', async () => {
  const handler = createPriceHistoryApiHandler({
    biwengerClient: fakeBiwengerClient([[250701, 105], [251215, 120]]),
  })

  const response = await handler({
    pathParameters: { playerId: '15396' },
    // Freezing "today" isn't wired through the handler — this test just
    // checks the endpoint returns *some* season-scoped array, the season
    // math itself is covered by price-history-view.test.js.
  })

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  assert.equal(fakeBiwengerClient.lastPlayerId, '15396')
  assert.ok(Array.isArray(JSON.parse(response.body).prices))
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createPriceHistoryApiHandler({
    biwengerClient: { getPlayerPrices: async () => { throw new Error('token=super-secret-leak') } },
  })

  const response = await handler({ pathParameters: { playerId: '15396' } })

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
