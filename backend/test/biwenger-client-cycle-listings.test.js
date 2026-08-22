import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createBiwengerClient } from '../src/biwenger-client.js'

test('cycleListings unlists the currently-listed players and lists the selected replacements, in one session', async () => {
  const calls = []
  const client = createBiwengerClient({
    baseUrl: 'https://example.test/api/v2',
    fetch: async (url, options = {}) => {
      calls.push({ url, options })
      if (url.endsWith('/auth/login')) return { ok: true, json: async () => ({ token: 'test-token' }) }
      if (url.endsWith('/account')) {
        return { ok: true, json: async () => ({ data: { leagues: [{ id: 7, user: { id: 42 } }], account: { credits: 0 } } }) }
      }
      if (url.includes('/user?fields=')) {
        return {
          ok: true,
          json: async () => ({
            data: {
              players: [
                { id: 10, owner: { lockedUntil: null, price: 100000, date: 1700000000 } }, // currently listed
                { id: 20, owner: { lockedUntil: null, price: 100000, date: 1700000000 } }, // has a standing offer
                { id: 30, owner: { lockedUntil: null, price: 100000, date: 1700000000 } }, // plain eligible
              ],
            },
          }),
        }
      }
      if (url.includes('/competitions/la-liga/data')) {
        return {
          ok: true,
          json: async () => ({
            data: {
              players: {
                10: { id: 10, name: 'Ten', position: 1, teamID: 1, price: 1 },
                20: { id: 20, name: 'Twenty', position: 1, teamID: 1, price: 1 },
                30: { id: 30, name: 'Thirty', position: 1, teamID: 1, price: 1 },
              },
            },
          }),
        }
      }
      if (url.endsWith('/market') && options.method === 'POST') return { ok: true, json: async () => ({ status: 200 }) }
      if (url.endsWith('/market')) {
        return {
          ok: true,
          json: async () => ({
            data: {
              sales: [{ player: { id: 10 }, user: { id: 42 } }],
              offers: [{ to: { id: 42 }, requestedPlayers: [20], amount: 500000 }],
            },
          }),
        }
      }
      return { ok: true, json: async () => ({ status: 200 }) } // unlist DELETE (?player=)
    },
  })

  const result = await client.cycleListings({ email: 'test@example.com', password: 'secret' })

  assert.deepEqual(result, { unlisted: [10], listed: [30, 20] })

  const unlistCall = calls.find((call) => call.url === 'https://example.test/api/v2/market?player=10')
  assert.equal(unlistCall.options.method, 'DELETE')

  const listCalls = calls.filter((call) => call.url === 'https://example.test/api/v2/market' && call.options.method === 'POST')
  assert.deepEqual(
    listCalls.map((call) => JSON.parse(call.options.body)),
    [
      { type: 'sell', player: 30, price: 35_000_000 },
      { type: 'sell', player: 20, price: 35_000_000 },
    ]
  )
})
