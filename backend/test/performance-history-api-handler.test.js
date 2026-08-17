import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createPerformanceHistoryApiHandler } from '../src/performance-history-api-handler.js'

const fakeBiwengerClient = (reports) => ({
  getPlayerGameweekPoints: async ({ playerId, season }) => {
    fakeBiwengerClient.lastPlayerId = playerId
    fakeBiwengerClient.lastSeason = season
    return reports
  },
})

test('returns a 200 JSON body with the gameweek points, shaped via toPerformanceHistoryView', async () => {
  const handler = createPerformanceHistoryApiHandler({
    biwengerClient: fakeBiwengerClient([{ match: { round: { short: 'R1' } }, points: { 5: 4 } }]),
  })

  const response = await handler({ pathParameters: { playerId: '15396' } })

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  assert.equal(fakeBiwengerClient.lastPlayerId, '15396')
  const body = JSON.parse(response.body)
  assert.deepEqual(body, { gameweeks: [{ matchDay: 1, points: 4 }] })
})

test('defaults to the current season when no season query param is given', async () => {
  const handler = createPerformanceHistoryApiHandler({ biwengerClient: fakeBiwengerClient([]) })

  await handler({ pathParameters: { playerId: '15396' } })

  assert.equal(fakeBiwengerClient.lastSeason, 'current')
})

test('passes season=previous through when requested', async () => {
  const handler = createPerformanceHistoryApiHandler({ biwengerClient: fakeBiwengerClient([]) })

  await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { season: 'previous' } })

  assert.equal(fakeBiwengerClient.lastSeason, 'previous')
})

test('treats any other season value as current', async () => {
  const handler = createPerformanceHistoryApiHandler({ biwengerClient: fakeBiwengerClient([]) })

  await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { season: 'bogus' } })

  assert.equal(fakeBiwengerClient.lastSeason, 'current')
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createPerformanceHistoryApiHandler({
    biwengerClient: { getPlayerGameweekPoints: async () => { throw new Error('token=super-secret-leak') } },
  })

  const response = await handler({ pathParameters: { playerId: '15396' } })

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
