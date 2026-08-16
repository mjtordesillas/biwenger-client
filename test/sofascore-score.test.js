import { test } from 'node:test'
import assert from 'node:assert/strict'
import { sofascoreBase, toSofaScoreRows } from '../src/sofascore-score.js'

test('maps a sofascore rating to its base points, per the reverse-engineered table', () => {
  assert.equal(sofascoreBase(6.4), 2)
  assert.equal(sofascoreBase(6.5), 2)
  assert.equal(sofascoreBase(6.9), 4)
  assert.equal(sofascoreBase(7.0), 5)
  assert.equal(sofascoreBase(8.4), 11)
  assert.equal(sofascoreBase(9.5), 14)
  assert.equal(sofascoreBase(10), 14)
})

test('floors at -6 below the lowest band', () => {
  assert.equal(sofascoreBase(4.9), -6)
  assert.equal(sofascoreBase(0), -6)
})

test('only the sofascore row when there are no bonus events', () => {
  const rows = toSofaScoreRows({ sofascore: 6.4 })

  assert.deepEqual(rows, [{ type: 'sofascore', rating: 6.4, points: 2 }])
})

test('adds a goal row, scaled by count and position', () => {
  const rows = toSofaScoreRows({ sofascore: 8.4, pos4: true, goals: 1 })

  assert.deepEqual(rows, [
    { type: 'sofascore', rating: 8.4, points: 11 },
    { type: 'goal', count: 1, points: 3 },
  ])
})

test('adds a penalty-goal row, flat regardless of position', () => {
  const rows = toSofaScoreRows({ sofascore: 6.7, pos3: true, goalsPenalty: 1 })

  assert.deepEqual(rows, [
    { type: 'sofascore', rating: 6.7, points: 3 },
    { type: 'penalty', count: 1, points: 3 },
  ])
})

test('adds an assist row', () => {
  const rows = toSofaScoreRows({ sofascore: 6.4, assists: 1 })

  assert.deepEqual(rows, [
    { type: 'sofascore', rating: 6.4, points: 2 },
    { type: 'assist', count: 1, points: 1 },
  ])
})

test('adds no row for a card, which has no separate effect in this format', () => {
  const rows = toSofaScoreRows({ sofascore: 5.8, redCard: 1 })

  assert.deepEqual(rows, [{ type: 'sofascore', rating: 5.8, points: -1 }])
})
