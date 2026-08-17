import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toPlayerView } from '../src/player-view.js'

test('shapes a raw catalogue player into the client view, building image URLs', () => {
  const view = toPlayerView({
    id: 25127,
    name: 'Ez Abde',
    teamID: 87,
    position: 4,
    altPositions: [3],
    price: 8330000,
    priceIncrement: 60000,
    points: 211,
  })

  assert.deepEqual(view, {
    id: 25127,
    name: 'Ez Abde',
    position: 4,
    secondaryPosition: 3,
    price: 8330000,
    priceIncrement: 60000,
    points: 211,
    photoUrl: 'https://cdn.biwenger.com/i/p/25127.png',
    teamCrestUrl: 'https://cdn.biwenger.com/i/t/87.png',
  })
})

test('defaults secondaryPosition to null when altPositions is absent', () => {
  const view = toPlayerView({
    id: 17731, name: 'Catena', teamID: 93, position: 2, price: 3820000, priceIncrement: 20000, points: 0,
  })

  assert.equal(view.secondaryPosition, null)
})

test('defaults priceIncrement and points to 0 when the catalogue omits them', () => {
  const view = toPlayerView({
    id: 1, name: 'Nobody', teamID: 1, position: 1, price: 100000,
  })

  assert.equal(view.priceIncrement, 0)
  assert.equal(view.points, 0)
})
