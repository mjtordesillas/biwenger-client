import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toPerformanceHistoryView } from '../src/performance-history-view.js'

test('maps reports to one gameweek per match day, using the "5" scoring format', () => {
  const reports = [
    { match: { round: { short: 'R1' } }, points: { 1: 2, 5: 1 } },
    { match: { round: { short: 'R2' } }, points: { 1: 2, 5: 2 } },
  ]

  const view = toPerformanceHistoryView(reports)

  assert.deepEqual(view, {
    gameweeks: [
      { matchDay: 1, points: 1 },
      { matchDay: 2, points: 2 },
    ],
  })
})

test('sorts gameweeks by match day, since rounds can arrive out of order (postponements)', () => {
  const reports = [
    { match: { round: { short: 'R24' } }, points: { 5: 3 } },
    { match: { round: { short: 'R16' } }, points: { 5: 4 } },
  ]

  const view = toPerformanceHistoryView(reports)

  assert.deepEqual(view.gameweeks.map((g) => g.matchDay), [16, 24])
})

test('skips rounds the player did not feature in (null report), and nulls out a missing score for the format', () => {
  const reports = [
    { match: { round: { short: 'R1' } }, points: { 5: 5 } },
    null,
    { match: { round: { short: 'R3' } }, points: { 1: 2, 5: null } },
  ]

  const view = toPerformanceHistoryView(reports)

  assert.deepEqual(view, {
    gameweeks: [
      { matchDay: 1, points: 5 },
      { matchDay: 3, points: null },
    ],
  })
})
