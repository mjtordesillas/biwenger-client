import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toMatchDayDetailsView } from '../src/match-day-details-view.js'

const report = ({ round, date, points, picas = 2, sofascore = 6.4, home, away }) => ({
  match: {
    round: { short: round },
    date,
    home: { id: home.id, name: home.name, score: home.score },
    away: { id: away.id, name: away.name, score: away.score },
  },
  points,
  rawStats: { picas, sofascore },
})

test('shapes the header, points totals, and AS/SofaScore base rows for the requested match day', () => {
  const reports = [
    report({
      round: 'R8',
      date: 1741604400,
      points: { 1: 6, 2: 2, 5: 4 },
      picas: 2,
      sofascore: 6.4,
      home: { id: 87, name: 'Betis', score: 2 },
      away: { id: 91, name: 'Alavés', score: 1 },
    }),
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 8 })

  assert.deepEqual(view, {
    matchDay: 8,
    kickoff: 1741604400,
    points: 4,
    home: { id: 87, name: 'Betis', score: 2, crestUrl: 'https://cdn.biwenger.com/i/t/87.png' },
    away: { id: 91, name: 'Alavés', score: 1, crestUrl: 'https://cdn.biwenger.com/i/t/91.png' },
    sofaScore: { points: 2, rows: [{ type: 'sofascore', rating: 6.4, points: 2 }] },
    as: { points: 6, rows: [{ type: 'picas', count: 2, points: 6 }] },
  })
})

test('nulls out the points total when the format is missing from the report', () => {
  const reports = [
    report({
      round: 'R8',
      date: 1741604400,
      points: { 1: 2 },
      home: { id: 87, name: 'Betis', score: 2 },
      away: { id: 91, name: 'Alavés', score: 1 },
    }),
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 8 })

  assert.equal(view.points, null)
})

test('includes AS bonus rows when the report has them', () => {
  const reports = [
    {
      match: {
        round: { short: 'R8' },
        date: 1741604400,
        home: { id: 87, name: 'Betis', score: 2 },
        away: { id: 91, name: 'Alavés', score: 1 },
      },
      points: { 1: 9 },
      rawStats: { picas: 2, pos4: true, goals: 1 },
    },
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 8 })

  assert.deepEqual(view.as, {
    points: 9,
    rows: [
      { type: 'picas', count: 2, points: 6 },
      { type: 'goal', count: 1, points: 3 },
    ],
  })
})

test('handles the "SC" (unrated) picas value in the AS block', () => {
  const reports = [
    report({
      round: 'R8',
      date: 1741604400,
      points: { 1: 0 },
      picas: 'SC',
      home: { id: 87, name: 'Betis', score: 2 },
      away: { id: 91, name: 'Alavés', score: 1 },
    }),
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 8 })

  assert.deepEqual(view.as.rows, [{ type: 'picas', count: 'SC', points: 0 }])
})

test('finds the requested match day among several reports', () => {
  const reports = [
    report({ round: 'R7', date: 1, home: { id: 1, name: 'A', score: 0 }, away: { id: 2, name: 'B', score: 0 } }),
    report({ round: 'R8', date: 2, home: { id: 3, name: 'C', score: 1 }, away: { id: 4, name: 'D', score: 1 } }),
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 8 })

  assert.equal(view.matchDay, 8)
  assert.equal(view.home.name, 'C')
})

test('skips rounds the player did not feature in (null report)', () => {
  const reports = [
    null,
    report({ round: 'R8', date: 2, home: { id: 3, name: 'C', score: 1 }, away: { id: 4, name: 'D', score: 1 } }),
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 8 })

  assert.equal(view.matchDay, 8)
})

test('returns null when the requested match day has no report', () => {
  const reports = [
    report({ round: 'R8', date: 2, home: { id: 3, name: 'C', score: 1 }, away: { id: 4, name: 'D', score: 1 } }),
  ]

  const view = toMatchDayDetailsView(reports, { matchDay: 9 })

  assert.equal(view, null)
})
