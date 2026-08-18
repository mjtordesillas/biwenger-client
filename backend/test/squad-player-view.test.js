import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toSquadPlayerView } from '../src/squad-player-view.js'

test('shapes a {player, owner, inMarket, offerAmount} tuple, carrying the transfer lock and fitness status', () => {
  const view = toSquadPlayerView({
    player: {
      id: 8747, name: 'Moncayola', teamID: 93, position: 3, price: 3320000, priceIncrement: -30000, points: 0, status: 'ok',
    },
    owner: { date: 1786943457, price: 3850123, lockedUntil: 1787202657 },
    inMarket: false,
    offerAmount: null,
  })

  assert.deepEqual(view, {
    id: 8747,
    name: 'Moncayola',
    position: 3,
    secondaryPosition: null,
    price: 3320000,
    priceIncrement: -30000,
    points: 0,
    photoUrl: 'https://cdn.biwenger.com/i/p/8747.png',
    teamCrestUrl: 'https://cdn.biwenger.com/i/t/93.png',
    lockedUntil: 1787202657,
    inMarket: false,
    offerAmount: null,
    status: 'ok',
  })
})

test('sets lockedUntil to null when owner has no lock (draft-owned, or past the lock)', () => {
  const view = toSquadPlayerView({
    player: { id: 15396, name: 'Brugué', teamID: 10, position: 4, price: 250000, status: 'sanctioned' },
    owner: { date: 1786573790 },
    inMarket: false,
    offerAmount: null,
  })

  assert.equal(view.lockedUntil, null)
})

test('carries inMarket/offerAmount through as given', () => {
  const view = toSquadPlayerView({
    player: { id: 8670, name: 'Roro Riquelme', teamID: 87, position: 3, price: 2030000, status: 'ok' },
    owner: { date: 1786683996, clause: 5960000 },
    inMarket: true,
    offerAmount: 2100000,
  })

  assert.equal(view.inMarket, true)
  assert.equal(view.offerAmount, 2100000)
})

test('defaults status to "unknown" when the catalogue omits it', () => {
  const view = toSquadPlayerView({
    player: { id: 1, name: 'Nobody', teamID: 1, position: 1, price: 100000 },
    owner: { date: 1786573790 },
    inMarket: false,
    offerAmount: null,
  })

  assert.equal(view.status, 'unknown')
})
