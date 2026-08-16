import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createMatchDayDetailsApiHandler } from '../src/match-day-details-api-handler.js'

const report = { match: { round: { short: 'R8' }, date: 1741604400, home: { id: 87, name: 'Betis', score: 2 }, away: { id: 91, name: 'Alavés', score: 1 } } }

const fakeBiwengerClient = (reports) => ({
  getPlayerGameweekPoints: async ({ playerId, season }) => {
    fakeBiwengerClient.lastPlayerId = playerId
    fakeBiwengerClient.lastSeason = season
    return reports
  },
})

test('returns a 200 JSON body with the match day header, shaped via toMatchDayDetailsView', async () => {
  const handler = createMatchDayDetailsApiHandler({ biwengerClient: fakeBiwengerClient([report]) })

  const response = await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { matchDay: '8' } })

  assert.equal(response.statusCode, 200)
  assert.equal(response.headers['Content-Type'], 'application/json; charset=utf-8')
  assert.equal(fakeBiwengerClient.lastPlayerId, '15396')
  const body = JSON.parse(response.body)
  assert.equal(body.matchDay, 8)
  assert.equal(body.home.name, 'Betis')
})

test('defaults to the current season when no season query param is given', async () => {
  const handler = createMatchDayDetailsApiHandler({ biwengerClient: fakeBiwengerClient([report]) })

  await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { matchDay: '8' } })

  assert.equal(fakeBiwengerClient.lastSeason, 'current')
})

test('passes season=previous through when requested', async () => {
  const handler = createMatchDayDetailsApiHandler({ biwengerClient: fakeBiwengerClient([report]) })

  await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { matchDay: '8', season: 'previous' } })

  assert.equal(fakeBiwengerClient.lastSeason, 'previous')
})

test('returns a 404 when the player has no report for the requested match day', async () => {
  const handler = createMatchDayDetailsApiHandler({ biwengerClient: fakeBiwengerClient([report]) })

  const response = await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { matchDay: '9' } })

  assert.equal(response.statusCode, 404)
})

test('returns a 502 with no upstream details when the Biwenger client fails', async () => {
  const handler = createMatchDayDetailsApiHandler({
    biwengerClient: { getPlayerGameweekPoints: async () => { throw new Error('token=super-secret-leak') } },
  })

  const response = await handler({ pathParameters: { playerId: '15396' }, queryStringParameters: { matchDay: '8' } })

  assert.equal(response.statusCode, 502)
  assert.doesNotMatch(response.body, /super-secret-leak/)
})
