import { test } from 'node:test'
import assert from 'node:assert/strict'
import { picasBase, goalBonus, toAsRows } from '../src/as-score.js'

test('maps picas to their base points, per the reverse-engineered table', () => {
  assert.equal(picasBase(0), -2)
  assert.equal(picasBase(1), 2)
  assert.equal(picasBase(2), 6)
  assert.equal(picasBase(3), 10)
  assert.equal(picasBase(4), 14)
})

test('maps the "SC" (unrated) picas value to 0', () => {
  assert.equal(picasBase('SC'), 0)
})

test('returns null for an unrecognized picas value', () => {
  assert.equal(picasBase(undefined), null)
})

test('scales the goal bonus by the position played that match', () => {
  assert.equal(goalBonus({ pos1: true }), 6)
  assert.equal(goalBonus({ pos2: true }), 5)
  assert.equal(goalBonus({ pos3: true }), 4)
  assert.equal(goalBonus({ pos4: true }), 3)
})

test('only the picas row when there are no bonus events', () => {
  const rows = toAsRows({ picas: 2 })

  assert.deepEqual(rows, [{ type: 'picas', count: 2, points: 6 }])
})

test('adds a goal row, scaled by count and position', () => {
  const rows = toAsRows({ picas: 2, pos3: true, goals: 2 })

  assert.deepEqual(rows, [
    { type: 'picas', count: 2, points: 6 },
    { type: 'goal', count: 2, points: 8 },
  ])
})

test('adds a penalty-goal row, flat regardless of position', () => {
  const rows = toAsRows({ picas: 2, pos4: true, goalsPenalty: 1 })

  assert.deepEqual(rows, [
    { type: 'picas', count: 2, points: 6 },
    { type: 'penalty', count: 1, points: 3 },
  ])
})

test('adds a red-card row', () => {
  const rows = toAsRows({ picas: 1, redCard: 1 })

  assert.deepEqual(rows, [
    { type: 'picas', count: 1, points: 2 },
    { type: 'redCard', count: 1, points: -6 },
  ])
})

test('adds a second-yellow-card row, distinct from a direct red card', () => {
  const rows = toAsRows({ picas: 1, secondYellowCard: 1 })

  assert.deepEqual(rows, [
    { type: 'picas', count: 1, points: 2 },
    { type: 'secondYellowCard', count: 1, points: -3 },
  ])
})

test('adds no row for a yellow card, which scores 0 in this format', () => {
  const rows = toAsRows({ picas: 2, yellowCard: 1 })

  assert.deepEqual(rows, [{ type: 'picas', count: 2, points: 6 }])
})

test('adds no row for an assist, which scores 0 in this format', () => {
  const rows = toAsRows({ picas: 2, assists: 1 })

  assert.deepEqual(rows, [{ type: 'picas', count: 2, points: 6 }])
})
