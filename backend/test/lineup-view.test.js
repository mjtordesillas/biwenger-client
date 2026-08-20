import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toLineupView } from '../src/lineup-view.js'

test('shapes a {formation, players, credits} triple, keeping the formation string and credits, shaping each player', () => {
  const view = toLineupView({
    formation: '3-5-2',
    credits: 20,
    players: [
      { id: 41101, name: 'Alfonso Herrero', teamID: 65, position: 1, price: 3880000, priceIncrement: -30000, points: 0 },
      { id: 40075, name: 'Xavi Espart', teamID: 3, position: 2, price: 410000 },
    ],
  })

  assert.deepEqual(view, {
    formation: '3-5-2',
    credits: 20,
    players: [
      {
        id: 41101,
        name: 'Alfonso Herrero',
        position: 1,
        secondaryPosition: null,
        price: 3880000,
        priceIncrement: -30000,
        points: 0,
        photoUrl: 'https://cdn.biwenger.com/i/p/41101.png',
        teamCrestUrl: 'https://cdn.biwenger.com/i/t/65.png',
      },
      {
        id: 40075,
        name: 'Xavi Espart',
        position: 2,
        secondaryPosition: null,
        price: 410000,
        priceIncrement: 0,
        points: 0,
        photoUrl: 'https://cdn.biwenger.com/i/p/40075.png',
        teamCrestUrl: 'https://cdn.biwenger.com/i/t/3.png',
      },
    ],
  })
})

test('preserves player order (Biwenger returns goalkeeper, then defenders/midfielders/forwards grouped)', () => {
  const view = toLineupView({
    formation: '4-4-2',
    players: [
      { id: 1, name: 'Keeper', teamID: 1, position: 1, price: 100 },
      { id: 2, name: 'Forward', teamID: 1, position: 4, price: 100 },
    ],
  })

  assert.deepEqual(view.players.map((player) => player.id), [1, 2])
})

test('keeps a vacant slot as null, at its index, rather than dropping it', () => {
  const view = toLineupView({
    formation: '4-4-2',
    players: [
      { id: 1, name: 'Keeper', teamID: 1, position: 1, price: 100 },
      null,
      { id: 3, name: 'Forward', teamID: 1, position: 4, price: 100 },
    ],
  })

  assert.equal(view.players.length, 3)
  assert.equal(view.players[1], null)
  assert.deepEqual(view.players.map((player) => player?.id), [1, undefined, 3])
})
